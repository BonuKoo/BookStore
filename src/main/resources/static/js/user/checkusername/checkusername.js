document.getElementById('checkUseBtn').addEventListener('click', function() {
    const username = document.getElementById('username').value.trim();

    // 현재 창이 팝업 창이라고 가정하고, 부모 창의 필드를 찾아 값을 설정합니다.
    if (window.opener && !window.opener.closed) {
        // 부모 창의 아이디 필드에 값을 설정
        window.opener.document.getElementById('username').value = username;

        // 팝업 창 닫기
        window.close();
    } else {
        alert('부모 창이 열려있지 않습니다.');
    }
});

document.getElementById('checkUsernameBtn').addEventListener('click', function() {
    const username = document.getElementById('username').value.trim();
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    // 아이디가 비어있다면
    if (username === '') {
        document.getElementById('modalMessage').innerText = '아이디를 입력하세요';
        document.getElementById('overlay').style.display = 'block';
        document.getElementById('modal').style.display = 'block';
        return; // POST 요청을 실행하지 않음
    }

    fetch('/check-username', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify({ username: username })
    })
    .then(response => response.json())
    .then(data => {
        document.getElementById('modalMessage').innerText = data.message;
        document.getElementById('overlay').style.display = 'block';
        document.getElementById('modal').style.display = 'block';

        // Check the availability and adjust the UI accordingly
        if (data.exists === false) {
            document.getElementById('checkUseBtn').style.display = 'inline-block'; // Show button if username is available
            document.getElementById('usernameAvailable').value = 'true';
        } else {
            document.getElementById('checkUseBtn').style.display = 'none'; // Hide button if username is not available
            document.getElementById('usernameAvailable').value = 'false';
        }
    })
    .catch(error => {
        console.error('Error:', error);
    });
});


document.getElementById('modalCloseBtn').addEventListener('click', function() {
    document.getElementById('overlay').style.display = 'none';
    document.getElementById('modal').style.display = 'none';
});


document.getElementById('overlay').addEventListener('click', function() {
    document.getElementById('overlay').style.display = 'none';
    document.getElementById('modal').style.display = 'none';
});
