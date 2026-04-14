// HÀM CHUYỂN TAB
function switchTab(tabId, menuItem) {
    // 1. Giấu hết các tab đi
    let tabs = document.querySelectorAll('.tab-content');
    tabs.forEach(tab => tab.classList.remove('active'));
    
    // 2. Tắt màu highlight của các nút menu
    let items = document.querySelectorAll('.menu-item');
    items.forEach(item => item.classList.remove('active'));
    
    // 3. Bật tab được chọn và highlight nút menu tương ứng
    document.getElementById(tabId).classList.add('active');
    menuItem.classList.add('active');

    if(tabId === 'tab-users') {
        fetchUsers(); // Gọi hàm lấy data từ Backend
    }

    if(tabId === 'tab-rfid') fetchRfidCards();

    if(tabId === 'tab-spots') fetchManageSpots();

    if(tabId === 'tab-dashboard') {
        fetchSpots();
        fetchHistory();
    }
}

// ================= KHỞI CHẠY ỨNG DỤNG =================
checkAuth();

// ================= TỰ ĐỘNG CẬP NHẬT DỮ LIỆU THẬT =================
setInterval(() => {
    if(sessionStorage.getItem('iot_parking_token')) {
        fetchHistory();
        fetchSpots();
    }
}, 2000);