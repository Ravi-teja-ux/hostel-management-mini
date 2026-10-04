"use strict";

const STUDENT_SESSION_KEY = "hostelStudentLoggedIn";
const STUDENT_ID_KEY = "hostelStudentId";
const ADMIN_SESSION_KEY = "hostelAdminLoggedIn";
const CURRENCY_FORMATTER = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  maximumFractionDigits: 2
});

function formatMoney(amount) {
  return CURRENCY_FORMATTER.format(Number(amount));
}

function getStudentId() {
  return sessionStorage.getItem(STUDENT_ID_KEY);
}

function setStudentIdentity(student) {
  document.querySelectorAll("[data-student-name]").forEach((element) => {
    element.textContent = student.name;
  });
  const initials = student.name.split(/\s+/).filter(Boolean).slice(0, 2)
    .map((part) => part[0].toLocaleUpperCase()).join("");
  document.querySelectorAll("[data-student-initials]").forEach((element) => {
    element.textContent = initials || "--";
  });
}

function showStudentError(message) {
  setApiMessage(document.querySelector("#student-page-message"), message, "error");
}

function updateFeeStatusBadge(element, status) {
  const statusKey = String(status).toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/(^-|-$)/g, "");
  element.className = `status-pill status-${statusKey}`;
  element.textContent = status;
}

async function loadStudentDashboard(studentId) {
  const results = await Promise.allSettled([
    apiRequest(`/api/students/${encodeURIComponent(studentId)}`),
    apiRequest(`/api/students/${encodeURIComponent(studentId)}/room`),
    apiRequest(`/api/students/${encodeURIComponent(studentId)}/fees`)
  ]);
  const errors = [];
  const [studentResult, roomResult, feeResult] = results;

  if (studentResult.status === "fulfilled") {
    const student = studentResult.value;
    setStudentIdentity(student);
    document.querySelector("#welcome-title").textContent = `Welcome back, ${student.name}.`;
  } else {
    errors.push(studentResult.reason.message);
  }

  if (roomResult.status === "fulfilled") {
    const room = roomResult.value;
    document.querySelector("#dashboard-room").textContent = `${room.block} · ${room.roomNumber}`;
  } else {
    document.querySelector("#dashboard-room").textContent = "No room assignment";
    errors.push(roomResult.reason.message);
  }

  if (feeResult.status === "fulfilled") {
    const fee = feeResult.value;
    document.querySelector("#dashboard-fee-status").textContent =
      `${fee.paymentStatus} · ${formatMoney(fee.pendingAmount)} pending`;
  } else {
    document.querySelector("#dashboard-fee-status").textContent = "Fee record unavailable";
    errors.push(feeResult.reason.message);
  }

  if (errors.length > 0) {
    showStudentError(`${[...new Set(errors)].join(" ")} Refresh this page to retry.`);
  }
}

function initializeStudentPage() {
  if (!document.body.classList.contains("app-page")) {
    return;
  }
  const studentId = getStudentId();
  if (sessionStorage.getItem(STUDENT_SESSION_KEY) !== "true" || !studentId) {
    window.location.replace("index.html");
    return;
  }
  setStudentIdentity({ name: sessionStorage.getItem("hostelStudentName") || studentId });

  document.querySelectorAll("[data-student-logout]").forEach((button) => {
    button.addEventListener("click", () => {
      sessionStorage.removeItem(STUDENT_SESSION_KEY);
      sessionStorage.removeItem(STUDENT_ID_KEY);
      window.location.assign("index.html");
    });
  });

  const page = document.body.dataset.studentPage;
  let load;
  if (page === "dashboard") {
    load = () => loadStudentDashboard(studentId);
  } else if (page === "room") {
    load = async () => {
      const [studentResult, roomResult] = await Promise.allSettled([
        apiRequest(`/api/students/${encodeURIComponent(studentId)}`),
        apiRequest(`/api/students/${encodeURIComponent(studentId)}/room`)
      ]);
      if (studentResult.status === "fulfilled") {
        setStudentIdentity(studentResult.value);
      }
      if (roomResult.status === "fulfilled") {
        loadStudentRoomFromResponse(roomResult.value);
      } else {
        document.querySelector("#room-title").textContent = "Room details unavailable";
        document.querySelectorAll("#room-summary, #room-block, #room-detail-number, #room-type, #room-capacity, #room-occupied-beds, #room-available-beds")
          .forEach((element) => { element.textContent = "Unavailable"; });
      }
      const errors = [studentResult, roomResult]
        .filter((result) => result.status === "rejected")
        .map((result) => result.reason.message);
      if (errors.length > 0) {
        showStudentError(`${[...new Set(errors)].join(" ")} Refresh this page to retry.`);
      }
    };
  } else if (page === "fees") {
    load = async () => {
      const [studentResult, feeResult] = await Promise.allSettled([
        apiRequest(`/api/students/${encodeURIComponent(studentId)}`),
        apiRequest(`/api/students/${encodeURIComponent(studentId)}/fees`)
      ]);
      if (studentResult.status === "fulfilled") {
        setStudentIdentity(studentResult.value);
      }
      if (feeResult.status === "fulfilled") {
        loadStudentFeesFromResponse(feeResult.value);
      } else {
        document.querySelector("#fee-summary-title").textContent = "Fee status unavailable";
        document.querySelector("#fee-summary-detail").textContent = "The fee record could not be loaded.";
        document.querySelector("#fee-summary-status").textContent = "Unavailable";
      }
      const errors = [studentResult, feeResult]
        .filter((result) => result.status === "rejected")
        .map((result) => result.reason.message);
      if (errors.length > 0) {
        showStudentError(`${[...new Set(errors)].join(" ")} Refresh this page to retry.`);
        if (feeResult.status === "rejected") {
          document.querySelector("#student-fee-row").innerHTML =
            `<tr><td colspan="4">${escapeText(feeResult.reason.message)}</td></tr>`;
        }
      }
    };
  } else if (page === "leave") {
    setStudentIdentity({ name: sessionStorage.getItem("hostelStudentName") || studentId });
  }

  if (load) {
    load().catch((error) => showStudentError(`${error.message} Refresh this page to retry.`));
  }
}

function loadStudentRoomFromResponse(room) {
  document.querySelector("#room-title").textContent = `${room.block} / Room ${room.roomNumber}`;
  document.querySelector("#room-summary").textContent = `${room.roomType} room · ${room.status}`;
  document.querySelector("#room-number").textContent = room.roomNumber;
  document.querySelector("#room-block").textContent = room.block;
  document.querySelector("#room-detail-number").textContent = room.roomNumber;
  document.querySelector("#room-type").textContent = room.roomType;
  document.querySelector("#room-capacity").textContent = `${room.capacity} beds`;
  document.querySelector("#room-occupied-beds").textContent = room.occupiedBeds;
  document.querySelector("#room-available-beds").textContent = room.availableBeds;
}

function loadStudentFeesFromResponse(fee) {
  document.querySelector("#fee-summary-title").textContent = `${formatMoney(fee.pendingAmount)} pending`;
  document.querySelector("#fee-summary-detail").textContent =
    `${formatMoney(fee.paidAmount)} paid of ${formatMoney(fee.totalFee)} total.`;
  updateFeeStatusBadge(document.querySelector("#fee-summary-status"), fee.paymentStatus);

  const row = document.createElement("tr");
  [fee.totalFee, fee.paidAmount, fee.pendingAmount].forEach((amount) => {
    const cell = document.createElement("td");
    cell.textContent = formatMoney(amount);
    row.append(cell);
  });
  const statusCell = document.createElement("td");
  const status = document.createElement("span");
  status.className = `table-status ${String(fee.paymentStatus).toLowerCase().replace(/[^a-z0-9]+/g, "-")}`;
  status.textContent = fee.paymentStatus;
  statusCell.append(status);
  row.append(statusCell);
  document.querySelector("#student-fee-row").replaceChildren(row);
}

function escapeText(value) {
  const element = document.createElement("span");
  element.textContent = value;
  return element.innerHTML;
}

const loginForm = document.querySelector("#login-form");

if (loginForm) {
  const roleInputs = loginForm.querySelectorAll('input[name="role"]');
  const idLabel = document.querySelector("#login-id-label");
  const studentIdInput = document.querySelector("#student-id");
  const passwordInput = document.querySelector("#password");
  const title = document.querySelector("#login-title");
  const supportingCopy = document.querySelector(".supporting-copy");
  const roleHint = document.querySelector("#login-role-hint");
  const submitButton = document.querySelector("#login-submit");
  const loginMessage = document.querySelector("#login-message");

  function selectedRole() {
    return loginForm.querySelector('input[name="role"]:checked').value;
  }

  function updateLoginRole() {
    const isAdmin = selectedRole() === "admin";
    idLabel.textContent = isAdmin ? "Admin ID" : "Student ID";
    studentIdInput.placeholder = isAdmin ? "Enter admin ID" : "e.g. 26215A0535";
    title.textContent = isAdmin ? "Admin login" : "Student login";
    supportingCopy.textContent = isAdmin
      ? "Sign in to manage students, rooms, fees, and leave requests."
      : "Enter your student details to view your hostel information.";
    roleHint.textContent = isAdmin
      ? "Sign in with your assigned administrator ID and password."
      : "Sign in with your assigned student ID and password.";
    submitButton.innerHTML = isAdmin
      ? 'Continue to admin portal <span aria-hidden="true">→</span>'
      : 'Continue to dashboard <span aria-hidden="true">→</span>';
    loginMessage.textContent = "";
    loginMessage.dataset.state = "";
  }

  roleInputs.forEach((input) => input.addEventListener("change", updateLoginRole));

  loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    if (!loginForm.reportValidity()) {
      return;
    }

    const role = selectedRole();
    const id = studentIdInput.value.trim();
    const password = passwordInput.value;
    const requestBody = role === "admin"
      ? { adminId: id, password }
      : { studentId: id, password };
    const path = role === "admin" ? "/api/admin/login" : "/api/student/login";
    setApiMessage(loginMessage, "Checking your login…", "info");
    setButtonLoading(submitButton, true, "Signing in…");

    try {
      const result = await apiRequest(path, {
        method: "POST",
        body: JSON.stringify(requestBody)
      });
      if (result.role === "ADMIN") {
        sessionStorage.setItem(ADMIN_SESSION_KEY, "true");
        sessionStorage.setItem("hostelAdminName", result.name);
        sessionStorage.removeItem(STUDENT_SESSION_KEY);
        sessionStorage.removeItem(STUDENT_ID_KEY);
        sessionStorage.removeItem("hostelStudentName");
        window.location.assign("admin-dashboard.html");
      } else {
        sessionStorage.setItem(STUDENT_SESSION_KEY, "true");
        sessionStorage.setItem(STUDENT_ID_KEY, result.id);
        sessionStorage.setItem("hostelStudentName", result.name);
        sessionStorage.removeItem(ADMIN_SESSION_KEY);
        sessionStorage.removeItem("hostelAdminName");
        window.location.assign("dashboard.html");
      }
    } catch (error) {
      setApiMessage(loginMessage, error.message, "error");
    } finally {
      setButtonLoading(submitButton, false);
    }
  });

  updateLoginRole();
}

const leaveForm = document.querySelector("#leave-form");

if (leaveForm) {
  const leaveDate = document.querySelector("#leave-date");
  const returnDate = document.querySelector("#return-date");
  const leaveReason = document.querySelector("#leave-reason");
  const leaveNote = document.querySelector("#leave-note");
  const message = document.querySelector("#leave-message");
  const submitButton = leaveForm.querySelector('button[type="submit"]');
  const localToday = new Date();
  const dateToday = new Date(localToday.getTime() - localToday.getTimezoneOffset() * 60_000)
    .toISOString()
    .slice(0, 10);

  leaveDate.min = dateToday;
  returnDate.min = dateToday;

  leaveDate.addEventListener("change", () => {
    returnDate.min = leaveDate.value || dateToday;
    if (returnDate.value && returnDate.value < returnDate.min) {
      returnDate.value = "";
    }
  });

  leaveForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    if (!leaveForm.reportValidity()) {
      return;
    }
    const studentId = getStudentId();
    const selectedReason = leaveReason.options[leaveReason.selectedIndex].text;
    const details = leaveNote.value.trim();
    const reason = details ? `${selectedReason}: ${details}` : selectedReason;
    if (reason.length > 500) {
      setApiMessage(message, "The reason and details must be 500 characters or fewer.", "error");
      return;
    }

    setApiMessage(message, "Submitting your leave request…", "info");
    setButtonLoading(submitButton, true, "Submitting…");
    try {
      await apiRequest("/api/leave-requests", {
        method: "POST",
        body: JSON.stringify({
          studentId,
          leaveDate: leaveDate.value,
          returnDate: returnDate.value,
          reason
        })
      });
      setApiMessage(message, "Your leave request was submitted successfully.", "success");
      leaveForm.reset();
      leaveDate.min = dateToday;
      returnDate.min = dateToday;
    } catch (error) {
      setApiMessage(message, error.message, "error");
    } finally {
      setButtonLoading(submitButton, false);
    }
  });
}

initializeStudentPage();
