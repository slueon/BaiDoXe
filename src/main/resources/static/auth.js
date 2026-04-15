// ================= LOGIC ĐĂNG NHẬP =================
function checkAuth() {
    const token = sessionStorage.getItem('iot_parking_token');
    const role = sessionStorage.getItem('iot_parking_role');
    const currentPath = window.location.pathname;

    // 1. Nếu chưa có token mà đang không ở trang login -> Đuổi về login
    if (!token && !currentPath.includes('login.html')) {
        window.location.href = 'login.html';
        return;
    }

    // 2. Nếu đã đăng nhập mà lại mở trang login -> Đẩy thẳng vào màn hình làm việc
    if (token && (currentPath.includes('login.html') || currentPath === '/' || currentPath === '/index.html')) {
        if (role === 'ADMIN') window.location.href = 'admin.html';
        else window.location.href = 'staff.html';
        return;
    }

    // 3. Bảo vệ trang Admin: Nếu không phải ADMIN mà mò vào admin.html -> Đuổi về staff
    if (token && role !== 'ADMIN' && currentPath.includes('admin.html')) {
        alert("Bạn không có quyền truy cập trang Quản trị!");
        window.location.href = 'staff.html';
        return;
    }

    // Hiển thị lời chào nếu có thẻ welcome-text
    const welcomeText = document.getElementById('welcome-text');
    if (welcomeText && role) {
        welcomeText.innerText = `Xin chào, ${role}`;
    }
    
    // Load dữ liệu bãi xe nếu có hàm
    if (token && typeof fetchSpots === 'function') fetchSpots();
}

async function handleLogin() {
    const user = document.getElementById('username').value;
    const pass = document.getElementById('password').value;
    const errorMsg = document.getElementById('login-error');
    try {
        const response = await fetch('http://localhost:8080/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: user, password: pass })
        });
        const data = await response.json();
        if (response.ok) {
            sessionStorage.setItem('iot_parking_token', data.token);
            sessionStorage.setItem('iot_parking_role', data.role); 
            errorMsg.style.display = 'none';
            
            // CHUYỂN HƯỚNG URL Ở ĐÂY DỰA TRÊN QUYỀN
            if (data.role === 'ADMIN') {
                window.location.href = 'admin.html';
            } else {
                window.location.href = 'staff.html';
            }
            checkAuth();
        } else {
            errorMsg.style.display = 'block';
            errorMsg.innerText = data.message || 'Sai tài khoản hoặc mật khẩu';
        }
    } catch (error) {
        errorMsg.style.display = 'block';
        errorMsg.innerText = 'Không kết nối được Server!';
    }
}

function handleLogout() {
    sessionStorage.clear();
    window.location.href = 'login.html'; 
}