"use strict";

const ADMIN_SESSION_KEY = "hostelAdminLoggedIn";
const adminMessage = document.querySelector("#admin-page-message");
const adminMain = document.querySelector(".admin-main");

function escapeHtml(value) {
  return String(value ?? "").replace(/[&<>"']/g, (character) => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#39;"
  })[character]);
}

function formatMoney(amount) {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 2
  }).format(Number(amount));
}

function statusClass(status) {
  return `admin-status-${String(status).toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/(^-|-$)/g, "")}`;
}

function setAdminMessage(message, state = "info") {
  setApiMessage(adminMessage, message, state);
}

function setAdminPageLoading(message) {
  setAdminMessage(message, "info");
}

function showAdminError(error) {
  setAdminMessage(`${error.message} Refresh the page to retry.`, "error");
}

function setAdminActionsDisabled(disabled) {
  adminMain.querySelectorAll("button:not([data-admin-logout])").forEach((button) => {
    button.disabled = disabled;
  });
}

function setTableLoading(tableBody, columns, message) {
  tableBody.innerHTML = `<tr><td colspan="${columns}">${escapeHtml(message)}</td></tr>`;
}

function renderEmptyRow(tableBody, columns, message) {
  tableBody.innerHTML = `<tr><td colspan="${columns}">${escapeHtml(message)}</td></tr>`;
}

function confirmAndRun(button, action) {
  setAdminActionsDisabled(true);
  setButtonLoading(button, true, "Saving…");
  return action().catch(showAdminError).finally(() => {
    setButtonLoading(button, false);
    setAdminActionsDisabled(false);
  });
}

async function loadDashboard() {
  setAdminPageLoading("Loading dashboard statistics…");
  const dashboard = await apiRequest("/api/admin/dashboard");
  document.querySelector("#total-students").textContent = dashboard.totalStudents;
  document.querySelector("#total-rooms").textContent = dashboard.totalRooms;
  document.querySelector("#occupied-rooms").textContent = dashboard.occupiedRooms;
  document.querySelector("#available-rooms").textContent = dashboard.availableRooms;
  document.querySelector("#pending-leaves").textContent = dashboard.pendingLeaveRequests;
  setAdminMessage("");
}

async function fetchStudents() {
  return apiRequest("/api/admin/students");
}

function renderStudents(students) {
  const tableBody = document.querySelector("#students-table-body");
  if (students.length === 0) {
    renderEmptyRow(tableBody, 8, "No student records yet. Select Add student to create one.");
    return;
  }
  tableBody.innerHTML = students.map((student) => `
    <tr>
      <td>${escapeHtml(student.studentId)}</td>
      <td>${escapeHtml(student.name)}</td>
      <td>${escapeHtml(student.course)}</td>
      <td>${escapeHtml(student.year)}</td>
      <td>${escapeHtml(student.roomNumber)}</td>
      <td>${escapeHtml(student.phone)}</td>
      <td><span class="admin-status ${statusClass(student.status)}">${escapeHtml(student.status)}</span></td>
      <td class="admin-actions"><div class="admin-actions-group">
        <button class="admin-button admin-button-edit" type="button" data-student-action="edit" data-record-id="${escapeHtml(student.id)}">Edit</button>
        <button class="admin-button admin-button-delete" type="button" data-student-action="delete" data-record-id="${escapeHtml(student.id)}">Delete</button>
      </div></td>
    </tr>`).join("");
}

async function loadStudents(showSuccess = false) {
  const tableBody = document.querySelector("#students-table-body");
  setTableLoading(tableBody, 8, "Loading students…");
  setAdminPageLoading("Loading student records…");
  const students = await fetchStudents();
  renderStudents(students);
  setAdminMessage(showSuccess ? "Student records updated." : "", showSuccess ? "success" : "info");
}

function promptValue(label, currentValue = "") {
  const value = window.prompt(label, currentValue);
  if (value === null) {
    return null;
  }
  if (!value.trim()) {
    throw new Error(`${label.replace(/:$/, "")} is required.`);
  }
  return value.trim();
}

function promptStudent(student = null) {
  const studentId = promptValue("Student ID:", student?.studentId || "");
  if (studentId === null) return null;
  const name = promptValue("Student name:", student?.name || "");
  if (name === null) return null;
  const course = promptValue("Course:", student?.course || "");
  if (course === null) return null;
  const yearText = promptValue("Year of study:", student?.year || "");
  if (yearText === null) return null;
  const year = Number(yearText);
  if (!Number.isInteger(year) || year < 1) {
    throw new Error("Year must be a positive whole number.");
  }
  const roomNumber = promptValue("Room number:", student?.roomNumber || "");
  if (roomNumber === null) return null;
  const phone = promptValue("Phone number:", student?.phone || "");
  if (phone === null) return null;
  const status = promptValue("Status (Active or Inactive):", student?.status || "Active");
  if (status === null) return null;
  if (!["active", "inactive"].includes(status.toLowerCase())) {
    throw new Error("Status must be Active or Inactive.");
  }
  const request = {
    studentId,
    name,
    course,
    year,
    roomNumber,
    phone,
    status: status.toLowerCase() === "active" ? "Active" : "Inactive"
  };

  if (!student) {
    const password = promptValue("Initial student password (at least 6 characters):");
    if (password === null) return null;
    if (password.length < 6) {
      throw new Error("The initial password must contain at least 6 characters.");
    }
    request.password = password;
  }
  return request;
}

function initializeStudentActions() {
  const tableBody = document.querySelector("#students-table-body");
  document.querySelector("#add-student").addEventListener("click", (event) => {
    const button = event.currentTarget;
    try {
      const request = promptStudent();
      if (!request) return;
      confirmAndRun(button, async () => {
        await apiRequest("/api/admin/students", {
          method: "POST",
          body: JSON.stringify(request)
        });
        await loadStudents(true);
      });
    } catch (error) {
      showAdminError(error);
    }
  });

  tableBody.addEventListener("click", (event) => {
    const button = event.target.closest("button[data-student-action]");
    if (!button) return;
    confirmAndRun(button, async () => {
      const students = await fetchStudents();
      const student = students.find((item) => String(item.id) === button.dataset.recordId);
      if (!student) throw new Error("That student record was not found. Refresh the page and try again.");

      if (button.dataset.studentAction === "delete") {
        if (!window.confirm(`Delete ${student.name} from the database?`)) return;
        await apiRequest(`/api/admin/students/${encodeURIComponent(student.id)}`, { method: "DELETE" });
        await loadStudents(true);
        return;
      }

      const request = promptStudent(student);
      if (!request) return;
      delete request.password;
      await apiRequest(`/api/admin/students/${encodeURIComponent(student.id)}`, {
        method: "PUT",
        body: JSON.stringify(request)
      });
      await loadStudents(true);
    });
  });
}

async function fetchRooms() {
  return apiRequest("/api/admin/rooms");
}

function renderRooms(rooms) {
  const tableBody = document.querySelector("#rooms-table-body");
  if (rooms.length === 0) {
    renderEmptyRow(tableBody, 8, "No room records yet. Select Add room to create one.");
    return;
  }
  tableBody.innerHTML = rooms.map((room) => `
    <tr>
      <td>${escapeHtml(room.roomNumber)}</td>
      <td>${escapeHtml(room.block)}</td>
      <td>${escapeHtml(room.roomType)}</td>
      <td>${escapeHtml(room.capacity)}</td>
      <td>${escapeHtml(room.occupiedBeds)}</td>
      <td>${escapeHtml(room.availableBeds)}</td>
      <td><span class="admin-status ${statusClass(room.status)}">${escapeHtml(room.status)}</span></td>
      <td class="admin-actions"><div class="admin-actions-group">
        <button class="admin-button admin-button-edit" type="button" data-room-action="edit" data-record-id="${escapeHtml(room.id)}">Edit room</button>
        <button class="admin-button admin-button-delete" type="button" data-room-action="delete" data-record-id="${escapeHtml(room.id)}">Delete room</button>
      </div></td>
    </tr>`).join("");
}

async function loadRooms(showSuccess = false) {
  const tableBody = document.querySelector("#rooms-table-body");
  setTableLoading(tableBody, 8, "Loading rooms…");
  setAdminPageLoading("Loading room records…");
  const rooms = await fetchRooms();
  renderRooms(rooms);
  setAdminMessage(showSuccess ? "Room records updated." : "", showSuccess ? "success" : "info");
}

function promptRoom(room = null) {
  const roomNumber = promptValue("Room number:", room?.roomNumber || "");
  if (roomNumber === null) return null;
  const block = promptValue("Block:", room?.block || "");
  if (block === null) return null;
  const roomType = promptValue("Room type:", room?.roomType || "");
  if (roomType === null) return null;
  const capacityText = promptValue("Bed capacity:", room?.capacity || "");
  if (capacityText === null) return null;
  const capacity = Number(capacityText);
  if (!Number.isInteger(capacity) || capacity < 1) {
    throw new Error("Room capacity must be a positive whole number.");
  }
  const occupiedText = promptValue("Occupied beds:", room?.occupiedBeds ?? 0);
  if (occupiedText === null) return null;
  const occupiedBeds = Number(occupiedText);
  if (!Number.isInteger(occupiedBeds) || occupiedBeds < 0 || occupiedBeds > capacity) {
    throw new Error("Occupied beds must be between 0 and the room capacity.");
  }
  return { roomNumber, block, roomType, capacity, occupiedBeds };
}

function initializeRoomActions() {
  const tableBody = document.querySelector("#rooms-table-body");
  document.querySelector("#add-room").addEventListener("click", (event) => {
    const button = event.currentTarget;
    try {
      const request = promptRoom();
      if (!request) return;
      confirmAndRun(button, async () => {
        await apiRequest("/api/admin/rooms", {
          method: "POST",
          body: JSON.stringify(request)
        });
        await loadRooms(true);
      });
    } catch (error) {
      showAdminError(error);
    }
  });

  tableBody.addEventListener("click", (event) => {
    const button = event.target.closest("button[data-room-action]");
    if (!button) return;
    confirmAndRun(button, async () => {
      const rooms = await fetchRooms();
      const room = rooms.find((item) => String(item.id) === button.dataset.recordId);
      if (!room) throw new Error("That room record was not found. Refresh the page and try again.");
      const url = `/api/admin/rooms/${encodeURIComponent(room.id)}`;
      if (button.dataset.roomAction === "delete") {
        if (!window.confirm(`Delete room ${room.roomNumber} in ${room.block}?`)) return;
        await apiRequest(url, { method: "DELETE" });
        await loadRooms(true);
        return;
      }
      const request = promptRoom(room);
      if (!request) return;
      await apiRequest(url, { method: "PUT", body: JSON.stringify(request) });
      await loadRooms(true);
    });
  });
}

async function loadFees(showSuccess = false) {
  const tableBody = document.querySelector("#fees-table-body");
  setTableLoading(tableBody, 7, "Loading fee records…");
  setAdminPageLoading("Loading fee records…");
  const fees = await apiRequest("/api/admin/fees");
  if (fees.length === 0) {
    renderEmptyRow(tableBody, 7, "No fee records were found.");
  } else {
    tableBody.innerHTML = fees.map((fee) => `
      <tr>
        <td>${escapeHtml(fee.studentId)}</td>
        <td>${escapeHtml(fee.studentName)}</td>
        <td>${formatMoney(fee.totalFee)}</td>
        <td>${formatMoney(fee.paidAmount)}</td>
        <td>${formatMoney(fee.pendingAmount)}</td>
        <td><span class="admin-status ${statusClass(fee.paymentStatus)}">${escapeHtml(fee.paymentStatus)}</span></td>
        <td class="admin-actions"><button class="admin-button admin-button-edit" type="button" data-fee-action="edit" data-record-id="${escapeHtml(fee.id)}">Update fee</button></td>
      </tr>`).join("");
  }
  setAdminMessage(showSuccess ? "Fee records updated." : "", showSuccess ? "success" : "info");
}

function initializeFeeActions() {
  document.querySelector("#fees-table-body").addEventListener("click", (event) => {
    const button = event.target.closest("button[data-fee-action]");
    if (!button) return;
    confirmAndRun(button, async () => {
      const fees = await apiRequest("/api/admin/fees");
      const fee = fees.find((item) => String(item.id) === button.dataset.recordId);
      if (!fee) throw new Error("That fee record was not found. Refresh the page and try again.");
      const totalFeeText = window.prompt("Total fee:", fee.totalFee);
      if (totalFeeText === null) return;
      const paidAmountText = window.prompt("Paid amount:", fee.paidAmount);
      if (paidAmountText === null) return;
      if (!totalFeeText.trim() || !paidAmountText.trim()) {
        throw new Error("Enter both the total fee and the paid amount.");
      }
      const totalFee = Number(totalFeeText);
      const paidAmount = Number(paidAmountText);
      if (!Number.isFinite(totalFee) || !Number.isFinite(paidAmount) || totalFee < 0 || paidAmount < 0) {
        throw new Error("Enter valid non-negative amounts.");
      }
      if (paidAmount > totalFee) {
        throw new Error("Paid amount cannot exceed total fee.");
      }
      await apiRequest(`/api/admin/fees/${encodeURIComponent(fee.id)}`, {
        method: "PUT",
        body: JSON.stringify({ totalFee, paidAmount })
      });
      await loadFees(true);
    });
  });
}

function formatDate(dateString) {
  const date = new Date(`${dateString}T00:00:00`);
  return Number.isNaN(date.getTime())
    ? String(dateString)
    : new Intl.DateTimeFormat("en-IN", { day: "numeric", month: "short", year: "numeric" }).format(date);
}

async function loadLeaveRequests(showSuccess = false) {
  const tableBody = document.querySelector("#leave-table-body");
  setTableLoading(tableBody, 7, "Loading leave requests…");
  setAdminPageLoading("Loading leave requests…");
  const requests = await apiRequest("/api/admin/leave-requests");
  if (requests.length === 0) {
    renderEmptyRow(tableBody, 7, "There are no leave requests.");
  } else {
    tableBody.innerHTML = requests.map((request) => {
      const actions = request.status === "Pending"
        ? `<div class="admin-actions-group">
            <button class="admin-button admin-button-approve" type="button" data-leave-action="approve" data-record-id="${escapeHtml(request.id)}">Approve</button>
            <button class="admin-button admin-button-reject" type="button" data-leave-action="reject" data-record-id="${escapeHtml(request.id)}">Reject</button>
          </div>`
        : `<span class="admin-status ${statusClass(request.status)}">${escapeHtml(request.status)}</span>`;
      return `
        <tr>
          <td>${escapeHtml(request.studentId)}</td>
          <td>${escapeHtml(request.studentName)}</td>
          <td>${escapeHtml(formatDate(request.leaveDate))}</td>
          <td>${escapeHtml(formatDate(request.returnDate))}</td>
          <td>${escapeHtml(request.reason)}</td>
          <td><span class="admin-status ${statusClass(request.status)}">${escapeHtml(request.status)}</span></td>
          <td class="admin-actions">${actions}</td>
        </tr>`;
    }).join("");
  }
  setAdminMessage(showSuccess ? "Leave requests updated." : "", showSuccess ? "success" : "info");
}

function initializeLeaveActions() {
  document.querySelector("#leave-table-body").addEventListener("click", (event) => {
    const button = event.target.closest("button[data-leave-action]");
    if (!button) return;
    confirmAndRun(button, async () => {
      const action = button.dataset.leaveAction;
      const suffix = action === "approve" ? "approve" : "reject";
      await apiRequest(`/api/admin/leave-requests/${encodeURIComponent(button.dataset.recordId)}/${suffix}`, {
        method: "PUT"
      });
      await loadLeaveRequests(true);
    });
  });
}

function initializeAdminPage() {
  if (!document.body.classList.contains("admin-page")) {
    return;
  }
  if (sessionStorage.getItem(ADMIN_SESSION_KEY) !== "true") {
    window.location.replace("admin-login.html");
    return;
  }

  const adminName = sessionStorage.getItem("hostelAdminName") || "Hostel Admin";
  document.querySelector(".admin-user > span:last-child").textContent = adminName;
  document.querySelectorAll("[data-admin-logout]").forEach((button) => {
    button.addEventListener("click", () => {
      sessionStorage.removeItem(ADMIN_SESSION_KEY);
      sessionStorage.removeItem("hostelAdminName");
      window.location.assign("admin-login.html");
    });
  });

  const page = document.body.dataset.adminPage;
  if (page === "dashboard") {
    loadDashboard().catch(showAdminError);
  } else if (page === "students") {
    initializeStudentActions();
    loadStudents().catch(showAdminError);
  } else if (page === "rooms") {
    initializeRoomActions();
    loadRooms().catch(showAdminError);
  } else if (page === "fees") {
    initializeFeeActions();
    loadFees().catch(showAdminError);
  } else if (page === "leave-requests") {
    initializeLeaveActions();
    loadLeaveRequests().catch(showAdminError);
  }
}

initializeAdminPage();
