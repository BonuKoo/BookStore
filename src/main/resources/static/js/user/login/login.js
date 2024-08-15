    /* 로그인 반환 바로 수행 */

function login() {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    fetch('http://localhost:8080/api/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: JSON.stringify({ username, password }),
    })
        .then(response => {
            response.json().then(function (data) {
                console.log(data);
                window.location.replace('/')
            })
        })
        .catch(error => {
            console.error('Error during login:', error);
        });
}

/* 시큐리티 관련 로직*/
/* 로그인 실패 후 '/'로 돌아가기 전까지 5초 간격 설정 */
function login() {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    fetch('http://localhost:8080/api/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: JSON.stringify({ username, password }),
    })
    .then(response => {
        if (response.status === 401) {
            alert("Authentication failed");
            setTimeout(() => {
                window.location.replace('/');
            }, 5000); // 5초 간격을 둡니다.
        } else {
            response.json().then(data => {
                console.log(data);
                window.location.replace('/');
            });
        }
    })
    .catch(error => {
        console.error('Error during login:', error);
    });
}
