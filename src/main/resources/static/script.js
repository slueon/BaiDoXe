// HÀM CHUYỂN TAB
function switchTab(tabId, menuItem) {
  // 1. Giấu hết các tab đi
  let tabs = document.querySelectorAll(".tab-content");
  tabs.forEach((tab) => tab.classList.remove("active"));

  // 2. Tắt màu highlight của các nút menu
  let items = document.querySelectorAll(".menu-item");
  items.forEach((item) => item.classList.remove("active"));

  // 3. Bật tab được chọn và highlight nút menu tương ứng
  document.getElementById(tabId).classList.add("active");
  menuItem.classList.add("active");

  if (tabId === "tab-users") {
    fetchUsers(); // Gọi hàm lấy data từ Backend
  }

  if (tabId === "tab-rfid") fetchRfidCards();

  if (tabId === "tab-spots") fetchManageSpots();

  if (tabId === "tab-dashboard") {
    fetchSpots();
    fetchHistory();
  }
}

// ================= LOGIC ĐĂNG NHẬP=================
checkAuth();

function checkAuth() {
  const token = localStorage.getItem("iot_parking_token");
  const role = localStorage.getItem("iot_parking_role");
  if (token) {
    document.getElementById("login-container").style.display = "none";
    document.getElementById("app-container").style.display = "flex";
    document.getElementById("welcome-text").innerText = `Xin chào, ${role}`;

    // Trả về tab Dashboard mặc định mỗi lần load
    document.getElementById("tab-dashboard").classList.add("active");

    if (role === "ADMIN") {
      document.getElementById("sidebar").style.display = "block";
    } else {
      document.getElementById("sidebar").style.display = "none";
    }
    fetchSpots();
    // fetchHistory(); (Tạm comment để màn hình clean)
  } else {
    document.getElementById("login-container").style.display = "flex";
    document.getElementById("app-container").style.display = "none";
  }
}

async function handleLogin() {
  const user = document.getElementById("username").value;
  const pass = document.getElementById("password").value;
  const errorMsg = document.getElementById("login-error");

  try {
    const response = await fetch("http://localhost:8080/api/users/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username: user, password: pass }),
    });
    const data = await response.json();
    if (response.ok) {
      localStorage.setItem("iot_parking_token", data.token);
      localStorage.setItem("iot_parking_role", data.role);
      errorMsg.style.display = "none";
      checkAuth();
    } else {
      errorMsg.style.display = "block";
      errorMsg.innerText = data.message || "Sai tài khoản hoặc mật khẩu!";
    }
  } catch (error) {
    errorMsg.style.display = "block";
    errorMsg.innerText = "Không kết nối được Server!";
  }
}

function handleLogout() {
  localStorage.clear();
  document.getElementById("username").value = "";
  document.getElementById("password").value = "";
  checkAuth();
}

// ================= LOGIC BÃI XE (GIỮ NGUYÊN API) =================
function fetchSpots() {
  if (!localStorage.getItem("iot_parking_token")) return;
  fetch("http://localhost:8080/api/spots")
    .then((res) => res.json())
    .then((data) => {
      let html = "";
      data.forEach((spot) => {
        html += `<div class="spot ${spot.isOccupied ? "full" : "empty"}">
                          <div>${spot.spotName}</div>
                          <div style="font-size: 14px; margin-top: 10px;">${spot.isOccupied ? "CÓ XE" : "TRỐNG"}</div>
                      </div>`;
      });
      document.getElementById("parking-lot").innerHTML = html;
    })
    .catch((err) => console.log(err));
}
setInterval(fetchSpots, 2000);

// ================= LOGIC QUẢN LÝ NHÂN SỰ (CRUD) =================

// Hàm này để gọi API lấy danh sách nhân viên
function fetchUsers() {
  fetch("http://localhost:8080/api/users")
    .then((res) => res.json()) //API trả về dạng JSON
    .then((data) => {
      let html = "";
      data.forEach((user) => {
        // Tô màu phân biệt sếp với lính cho dễ nhìn
        let roleColor = user.role === "ADMIN" ? "#ff4d4d" : "#00ff88";
        html += `
                      <tr>
                          <td>${user.userId || "Auto"}</td>
                          <td>${user.username}</td>
                          <td>${user.fullName || "Chưa cập nhật"}</td>
                          <td><span style="color:${roleColor}; font-weight:bold;">${user.role}</span></td>
                          <td>
                              <button class="btn-edit" onclick="editUser(${user.userId}, '${user.fullName}', '${user.role}')">Sửa</button>
                              <button class="btn-delete" onclick="deleteUser(${user.userId}, '${user.username}')">Xóa</button>
                          </td>
                      </tr>
                  `;
      });
      document.getElementById("users-table").innerHTML = html;
    })
    .catch((err) => console.log("Lỗi lấy danh sách nhân viên:", err));
}

// Hàm này xử lý khi bấm nút "Thêm Nhân Viên"
async function addNewUser() {
  // Dùng hộp thoại popup có sẵn của trình duyệt để nhập liệu cho lẹ
  let username = prompt("Nhập tên đăng nhập (VD: baove2):");
  if (!username) return;

  let password = prompt("Nhập mật khẩu (VD: 12345):");
  if (!password) return;

  let fullName = prompt("Nhập Họ và Tên:");

  // Gói data lại phi sang Backend
  let newUserData = {
    username: username,
    passwordHash: password, // Chỗ này map đúng tên biến passwordHash của Entity Java nhé
    fullName: fullName,
    role: "STAFF", // Mặc định thêm mới là nhân viên quèn thôi
  };

  try {
    const response = await fetch("http://localhost:8080/api/users", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newUserData),
    });

    const data = await response.json();
    alert(data.message); // Báo kết quả

    if (response.ok) {
      fetchUsers(); // Thêm thành công thì load lại cái bảng ngay cho nóng!
    }
  } catch (error) {
    alert("Lỗi kết nối Backend rồi sếp ơi!");
  }
}

// Hàm xử lý bấm nút Xóa
async function deleteUser(id, username) {
  // Hỏi lại cho chắc cốp, lỡ sếp bấm nhầm
  if (!confirm(`Có chắc muốn xóa [${username}] không?`)) return;

  try {
    const response = await fetch(`http://localhost:8080/api/users/${id}`, {
      method: "DELETE",
    });
    const data = await response.json();
    alert(data.message);
    if (response.ok) fetchUsers(); // Xóa xong load lại bảng
  } catch (error) {
    alert("Lỗi không kết nối được Backend!");
  }
}

// Hàm xử lý bấm nút Sửa
async function editUser(id, oldName, oldRole) {
  // Hiện popup hỏi thông tin mới
  let newName = prompt("Nhập Họ và Tên mới:", oldName);
  if (newName === null) return; // Bấm Cancel thì hủy

  let newRole = prompt("Nhập Quyền mới (ADMIN hoặc STAFF):", oldRole);
  if (newRole === null) return;

  let newPassword = prompt(
    "Nhập Mật khẩu mới (Bỏ trống nếu không muốn đổi):",
    "",
  );

  let updateData = {
    fullName: newName,
    role: newRole.toUpperCase(),
  };
  if (newPassword) updateData.passwordHash = newPassword;

  try {
    const response = await fetch(`http://localhost:8080/api/users/${id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(updateData),
    });
    const data = await response.json();
    alert(data.message);
    if (response.ok) fetchUsers(); // Sửa xong load lại bảng
  } catch (error) {
    alert("Lỗi không kết nối được Backend!");
  }
}

// ================= LOGIC QUẢN LÝ THẺ RFID =================
function fetchRfidCards() {
  fetch("http://localhost:8080/api/rfid")
    .then((res) => res.json())
    .then((data) => {
      let html = "";
      data.forEach((card) => {
        const isCurrentlyActive =
          card.active !== undefined ? card.active : card.isActive;
        const statusColor = isCurrentlyActive ? "#00ff88" : "#ff4d4d";
        const statusText = isCurrentlyActive ? "HOẠT ĐỘNG" : "BỊ KHÓA";
        const toggleBtnText = isCurrentlyActive ? "Khóa" : "Mở";
        const toggleBtnClass = isCurrentlyActive ? "btn-edit" : "btn-add"; // Dùng màu cam hoặc xanh cho toggle

        html += `
    <tr>
      <td><strong style="color: #f39c12;">${card.cardId}</strong></td>
      <td><span style="color:${statusColor}; font-weight:bold;">${statusText}</span></td>
      <td>
        <button class="${toggleBtnClass}" onclick="toggleCardStatus('${card.cardId}')">
          ${toggleBtnText}
        </button>
        <button class="btn-delete" onclick="deleteRfidCard('${card.cardId}')" style="margin-left: 5px;">
          Xóa
        </button>
      </td>
    </tr>
  `;
      });
      document.getElementById("rfid-table").innerHTML = html;
    })
    .catch((err) => console.log("Lỗi:", err));
}

async function toggleCardStatus(cardId) {
  if (!confirm(`Xác nhận thay đổi trạng thái khóa/mở cho thẻ: ${cardId}?`))
    return;

  try {
    // Gọi API đảo ngược trạng thái (đảm bảo cardId viết đúng hoa/thường như PathVariable ở Java)
    const response = await fetch(
      `http://localhost:8080/api/rfid/${cardId}/toggle`,
      {
        method: "PUT",
      },
    );

    const data = await response.json();

    if (response.ok) {
      alert(data.message || "Đã cập nhật trạng thái thẻ!");
      fetchRfidCards(); // Cập nhật lại giao diện ngay lập tức
    } else {
      alert("Lỗi: " + (data.message || "Không thể thực hiện thao tác"));
    }
  } catch (error) {
    console.error("Lỗi kết nối:", error);
    alert("Lỗi kết nối Backend rồi sếp ơi!");
  }
}

async function deleteRfidCard(cardId) {
  // Hỏi kỹ kẻo xóa nhầm thì dữ liệu ra vào của thẻ đó có thể bị ảnh hưởng
  if (
    !confirm(
      `CẢNH BÁO: Sếp có chắc chắn muốn XÓA VĨNH VIỄN thẻ [${cardId}] không?`,
    )
  )
    return;

  try {
    const response = await fetch(`http://localhost:8080/api/rfid/${cardId}`, {
      method: "DELETE",
    });

    const data = await response.json();

    if (response.ok) {
      alert(data.message || "Đã xóa thẻ thành công!");
      fetchRfidCards(); // Xóa xong load lại bảng luôn
    } else {
      alert("Lỗi: " + (data.message || "Không thể xóa thẻ này"));
    }
  } catch (error) {
    console.error("Lỗi kết nối:", error);
    alert("Không kết nối được Server để xóa thẻ!");
  }
}
async function addNewRfidCard() {
  let uid = prompt("Nhập mã Thẻ (VD: CARD_VIP_02):");
  if (!uid) return;

  let newCardData = {
    cardId: uid,
    status: "ACTIVE",
  };

  try {
    const response = await fetch("http://localhost:8080/api/rfid", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newCardData),
    });
    const data = await response.json();
    alert(data.message);
    if (response.ok) fetchRfidCards();
  } catch (error) {
    alert("Lỗi không kết nối được Backend!");
  }
}

// ================= LOGIC QUẢN LÝ VỊ TRÍ ĐỖ =================
function fetchManageSpots() {
  fetch("http://localhost:8080/api/spots")
    .then((res) => res.json())
    .then((data) => {
      let html = "";
      data.forEach((spot) => {
        let statusText = spot.isOccupied
          ? '<span style="color:#ff4d4d; font-weight:bold;">CÓ XE</span>'
          : '<span style="color:#00ff88; font-weight:bold;">TRỐNG</span>';
        let currentId = spot.spotId || spot.id;

        let sensorDisplay = spot.sensorId
          ? `<strong style="color:#f39c12">${spot.sensorId}</strong>`
          : '<i style="color:gray">Chưa gắn</i>';

        html += `
                      <tr>
                          <td>${currentId}</td>
                          <td><strong style="font-size: 16px;">${spot.spotName}</strong></td>
                          <td>${sensorDisplay}</td> <td>${statusText}</td>
                          <td>
                              <button class="btn-delete" onclick="deleteSpot(${currentId}, '${spot.spotName}')">Xóa Ô Này</button>
                          </td>
                      </tr>
                  `;
      });
      document.getElementById("manage-spots-table").innerHTML = html;
    })
    .catch((err) => console.log("Lỗi lấy bãi đỗ:", err));
}

async function addNewSpot() {
  let spotName = prompt("Nhập tên ô đỗ mới (VD: Ô số 5):");
  if (!spotName) return;

  // BƠM THÊM ĐOẠN NÀY: Hỏi luôn mã cảm biến
  let sensorIdInput = prompt(
    "Nhập mã Cảm Biến gắn ở ô này (VD: S5).\nLưu ý: Không được nhập trùng với ô khác!",
  );

  // Đóng gói gửi xuống Backend
  let newSpotData = {
    spotName: spotName,
    isOccupied: false,
    sensorId: sensorIdInput || null, // Nhét thêm thằng sensorId vào đây
  };

  try {
    const response = await fetch("http://localhost:8080/api/spots", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newSpotData),
    });
    const data = await response.json();
    alert(data.message || "Thành công!");

    if (response.ok) {
      fetchManageSpots();
      fetchSpots();
    }
  } catch (error) {
    alert("Lỗi kết nối Backend!");
  }
}

async function deleteSpot(id, name) {
  if (!confirm(`Sếp có chắc chắn muốn đập bỏ [${name}] không?`)) return;
  try {
    const response = await fetch(`http://localhost:8080/api/spots/${id}`, {
      method: "DELETE",
    });
    const data = await response.json();
    alert(data.message || "Đã xóa!");
    if (response.ok) {
      fetchManageSpots();
      fetchSpots();
    }
  } catch (error) {
    alert("Lỗi kết nối Backend!");
  }
}

// ================= LOGIC VẬN HÀNH BÃI XE (QUẸT THẺ & LỊCH SỬ) =================
let myChart = null; // Biến lưu trữ cái biểu đồ

// Hàm giả lập quẹt thẻ
async function simulateScan() {
  let cardId = prompt("Hãy nhập mã Thẻ RFID (VD: CARD_111) để quẹt:");
  if (!cardId) return;

  try {
    // Gọi API scan thẻ nãy anh em mình vừa code bên RfidController
    const response = await fetch(
      `http://localhost:8080/api/rfid/scan/${cardId}`,
      { method: "POST" },
    );
    const data = await response.json();

    // Báo kết quả (Mở barie, thu tiền...)
    alert(data.message);

    // Quẹt thẻ xong thì tự động load lại Lịch sử và Sơ đồ bãi xe
    fetchHistory();
    fetchSpots();
  } catch (error) {
    alert("Lỗi không kết nối được Backend lúc quẹt thẻ!");
  }
}

// Hàm lấy lịch sử và vẽ biểu đồ
function fetchHistory() {
  fetch("http://localhost:8080/api/history")
    .then((res) => res.json())
    .then((data) => {
      let html = "";
      let revenueData = {};

      let reversedData = [...data].reverse();

      reversedData.forEach((row) => {
        let inTime = row.entryTime
          ? new Date(row.entryTime).toLocaleTimeString("vi-VN")
          : "-";
        let outTime = row.exitTime
          ? new Date(row.exitTime).toLocaleTimeString("vi-VN")
          : "-";
        let statusHtml =
          row.status === "IN"
            ? '<span style="color:#00ff88;">Đang gửi</span>'
            : '<span style="color:#ff4444;">Đã ra</span>';
        let fee = row.fee ? row.fee + " đ" : "-";

        // lấy Mã thẻ (cardId)
        let cardDisplay =
          row.rfidCard && row.rfidCard.cardId
            ? row.rfidCard.cardId
            : "Khách lạ";

        html += `<tr><td><strong style="color: #00d2ff;">${cardDisplay}</strong></td><td>${inTime}</td><td>${outTime}</td><td>${statusHtml}</td><td>${fee}</td></tr>`;
        // ===================================================

        // LOGIC TÍNH TIỀN DOANH THU
        if (row.status === "OUT" && row.fee) {
          let dateKey = new Date(row.exitTime).toLocaleDateString("vi-VN");
          if (revenueData[dateKey]) {
            revenueData[dateKey] += parseFloat(row.fee);
          } else {
            revenueData[dateKey] = parseFloat(row.fee);
          }
        }
      });

      document.getElementById("history-table").innerHTML = html;
      drawChart(Object.keys(revenueData), Object.values(revenueData));
    })
    .catch((err) => console.log("Lỗi kéo lịch sử:", err));
}

// 2. Hàm gọi thư viện Chart.js để vẽ
function drawChart(labels, dataPoints) {
  const canvas = document.getElementById("revenueChart");
  if (!canvas) return; // Không có chỗ vẽ thì nghỉ

  const ctx = canvas.getContext("2d");

  // Nếu có biểu đồ cũ rồi thì đập đi xây lại cái mới cho khỏi đè lên nhau
  if (myChart != null) {
    myChart.destroy();
  }

  // Bắt đầu vẽ
  myChart = new Chart(ctx, {
    type: "bar", // Vẽ biểu đồ cột (bar)
    data: {
      labels: labels, // Trục X: Các ngày
      datasets: [
        {
          label: "Doanh thu (VNĐ)",
          data: dataPoints, // Trục Y: Tổng tiền mỗi ngày
          backgroundColor: "rgba(243, 156, 18, 0.8)", // Màu cột (Cam)
          borderColor: "rgba(243, 156, 18, 1)",
          borderWidth: 1,
          borderRadius: 5,
        },
      ],
    },
    options: {
      scales: {
        y: { beginAtZero: true, ticks: { color: "white" } },
        x: { ticks: { color: "white" } },
      },
      plugins: { legend: { labels: { color: "white" } } },
    },
  });
}

// Hàm điều khiển Đóng/Mở Barie khẩn cấp
async function toggleBarrier(gate, btnElement) {
  const isEntrance = gate === "in";
  const gateCode = isEntrance ? "1" : "2"; // 1 cho cổng vào, 2 cho cổng ra
  const currentText = btnElement.innerText;

  let command = "";
  let nextText = "";
  let nextColor = "";

  // Kiểm tra trạng thái hiện tại dựa trên chữ hiển thị trên nút
  if (currentText.includes("Mở")) {
    command = "O" + gateCode; // Lệnh Open: O1 hoặc O2
    nextText = `Đóng Barrie đường ${isEntrance ? "vào" : "ra"}`;
    nextColor = "#2c3e50"; // Đổi sang màu tối khi trạng thái là "Đang Mở"
  } else {
    command = "C" + gateCode; // Lệnh Close: C1 hoặc C2
    nextText = `Mở Barrie đường ${isEntrance ? "vào" : "ra"}`;
    nextColor = isEntrance ? "#e67e22" : "#e74c3c"; // Trả lại màu cam/đỏ gốc
  }

  try {
    // Gọi API gửi mã lệnh O1, C1, O2, hoặc C2
    const response = await fetch(
      `http://localhost:8080/api/barrier/emergency/${command}`,
      {
        method: "POST",
      },
    );
    const data = await response.json();

    if (data.success) {
      // Cập nhật giao diện nút sau khi gửi lệnh thành công
      btnElement.innerText = nextText;
      btnElement.style.backgroundColor = nextColor;
      console.log(
        `Đã gửi thành công lệnh ${command} tới /ptit/servo/emergency`,
      );
    } else {
      alert("Lỗi: " + data.message);
    }
  } catch (error) {
    alert("Mất kết nối với Backend!");
  }
}

// ================= TỰ ĐỘNG CẬP NHẬT DỮ LIỆU THẬT =================
// Cứ 2 giây, Web sẽ tự động gọi Backend để lấy lịch sử và sơ đồ bãi xe mới nhất
setInterval(fetchHistory, 2000);
setInterval(fetchSpots, 2000);
