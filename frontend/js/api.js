"use strict";

const API_BASE_URL = window.location.port === "5500"
  ? `${window.location.protocol}//${window.location.hostname}:8080`
  : "";

async function apiRequest(path, options = {}) {
  const controller = new AbortController();
  const timeout = window.setTimeout(() => controller.abort(), 15000);
  const headers = new Headers(options.headers || {});
  const accessToken = sessionStorage.getItem("hostelAccessToken");
  if (accessToken) {
    headers.set("Authorization", `Bearer ${accessToken}`);
  }
  if (options.body !== undefined) {
    headers.set("Content-Type", "application/json");
  }

  try {
    const response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers,
      signal: controller.signal
    });
    const responseText = await response.text();
    let data = null;
    if (responseText) {
      try {
        data = JSON.parse(responseText);
      } catch {
        data = null;
      }
    }

    if (!response.ok) {
      if (response.status === 401 && accessToken && !path.endsWith("/login")) {
        sessionStorage.clear();
        window.location.assign("index.html");
      }
      const statusMessages = {
        400: "Some details are invalid. Check the form and try again.",
        401: "That ID or password is incorrect. Check it and try again.",
        403: "You do not have permission to do that.",
        404: "The requested record could not be found.",
        409: "This record conflicts with existing data. Check the details and try again.",
        500: "The server encountered a problem. Please try again in a moment."
      };
      const message = data?.message || data?.detail || data?.title
        || statusMessages[response.status]
        || `The request could not be completed (HTTP ${response.status}).`;
      throw new Error(message);
    }
    if (responseText && data === null) {
      throw new Error("The server returned an unreadable response. Refresh the page and try again.");
    }
    return data;
  } catch (error) {
    if (error.name === "AbortError") {
      throw new Error("The server took too long to respond. Check that it is running, then try again.");
    }
    if (error instanceof TypeError) {
      throw new Error("Could not reach the server. Check your connection and try again.");
    }
    throw error;
  } finally {
    window.clearTimeout(timeout);
  }
}

function setApiMessage(element, message, state = "info") {
  if (!element) {
    return;
  }
  element.textContent = message;
  element.dataset.state = state;
}

function setButtonLoading(button, loading, loadingText = "Please wait…") {
  if (!button) {
    return;
  }
  if (loading) {
    button.dataset.originalContent = button.innerHTML;
    button.disabled = true;
    button.textContent = loadingText;
  } else {
    button.disabled = false;
    if (button.dataset.originalContent) {
      button.innerHTML = button.dataset.originalContent;
      delete button.dataset.originalContent;
    }
  }
}
