
document.getElementById('checkUsernameBtn').addEventListener('click', function() {
    const username = document.getElementById('username').value.trim();
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    // 사용자 이름 입력 검증
    const usernamePattern = /^[A-Za-z0-9]+$/;
    if (!usernamePattern.test(username)) {
        document.getElementById('modalMessage').innerText = '아이디는 영어와 숫자만 사용 가능합니다';
        document.getElementById('overlay').style.display = 'block';
        document.getElementById('modal').style.display = 'block';
        return; // POST 요청을 실행하지 않음
    }

    // 사용자 이름 길이 검증
    if (username.length < 7 || username.length > 15) {
        document.getElementById('modalMessage').innerText = '아이디는 7자 이상 15자 이하로 입력해야 합니다';
        document.getElementById('overlay').style.display = 'block';
        document.getElementById('modal').style.display = 'block';
        return; // POST 요청을 실행하지 않음
    }

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

        // 아이디 사용 가능 여부를 확인하고 UI를 조정합니다
        if (data.exists === false) {
            document.getElementById('checkUseBtn').style.display = 'inline-block'; // 사용자 이름이 사용 가능한 경우 버튼 표시
            document.getElementById('usernameAvailable').value = 'true';
        } else {
            document.getElementById('checkUseBtn').style.display = 'none'; // 사용자 이름이 사용 불가능한 경우 버튼 숨김
            document.getElementById('usernameAvailable').value = 'false';
        }
    })
    .catch(error => {
        console.error('오류:', error);
    });
});



//loginForm에 값 입력
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
//닫기
document.getElementById('modalCloseBtn').addEventListener('click', function() {
    document.getElementById('overlay').style.display = 'none';
    document.getElementById('modal').style.display = 'none';
});

//나타나기
document.getElementById('overlay').addEventListener('click', function() {
    document.getElementById('overlay').style.display = 'none';
    document.getElementById('modal').style.display = 'none';
});
