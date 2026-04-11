function fetchSpots() {
    if (!localStorage.getItem('iot_parking_token')) return;
    fetch('http://localhost:8080/api/spots')
        .then(res => res.json())
        .then(data => {
            let html = '';
            data.forEach(spot => {
                html += `<div class="spot ${spot.isOccupied ? 'full' : 'empty'}">
                        <div>${spot.spotName}</div>
                        <div style="font-size: 14px; margin-top: 10px;">${spot.isOccupied ? 'CÓ XE' : 'TRỐNG'}</div>
                    </div>`;
            });
            document.getElementById('parking-lot').innerHTML = html;
        }).catch(err => console.log(err));
}
setInterval(fetchSpots, 2000);

async function simulateScan() {
    let cardId = prompt("Hãy nhập mã Thẻ RFID (VD: CARD_111) để quẹt:");
    if (!cardId) return;

    try {
        const response = await fetch(`http://localhost:8080/api/rfid/scan/${cardId}`, { method: 'POST' });
        const data = await response.json();
        alert(data.message);
        fetchHistory();
        fetchSpots();
    } catch (error) {
        alert("Lỗi không kết nối được Backend lúc quẹt thẻ!");
    }
}
