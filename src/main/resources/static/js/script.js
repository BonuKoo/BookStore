    // 이메일 전송
    function sendMail() {
    console.log("되라");
        const form = document.inquireForm;

        var title;
        const to = "zxcqew32@naver.com";
        var text;

    title = form.name.value + "님이 문의하신 내용입니다.";

    text = `<table>
    <tr><td>제목</td><td>${form.title.value}</td></tr>
    <tr><td>이메일</td><td>${form.email.value}</td>></tr>
    <tr><td>이름</td><td>${form.name.value}</td>></tr>
    <tr><td>전화번호</td><td>${form.phoneNum.value}</td>></tr>
    <tr><td>내용</td><td>${form.content.value}</td>></tr>
    </table>`;

    var titleInput = document.createElement('input');
    titleInput.type = 'hidden';
    titleInput.name = 'title';
    titleInput.value = title;

    var toInput = document.createElement('input');
    toInput.type = 'hidden';
    toInput.name = 'to';
    toInput.value = to;

    var textInput = document.createElement('input');
    textInput.type = 'hidden';
    textInput.name = 'text';
    textInput.value = text;
    console.log(text);

    form.appendChild(titleInput);
    form.appendChild(toInput);
    form.appendChild(textInput);

    form.submit();

    }
    //비밀번호 찾기
    function findPw(){
        window.open('/findpw','비밀번호 찾기', 'width=420,height=420,scrollbars=no,resizable=no');
    }

    function addFile(button) {
    	const parent = button.parentElement;
    	var addEl = document.createElement('p');
    	addEl.innerHTML = '<input type="file" name="file">'
    					+ '<input type="checkbox">대표이미지'
    					+ '<input type="button" value="X" onclick="delFile(this)">';
    	parent.appendChild(addEl);
    }

    function delFile(button) {
    	button.parentElement.remove();
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
    /* 로그 아웃 */
        function logout() {

            fetch('/api/logout', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                }
            })
                .then(response => {
                    if(response.ok){
                        window.location.replace('/api')
                    }
                })
                .catch(error => {
                    console.error('Error during login:', error);
                });
        }


// == 장바구니 아이템 관련 로직 == //

function count(type, itemIsbn) {
    // CSRF 토큰과 헤더 이름 가져오기
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;

    // 디버깅을 위해 변수 값 출력
    console.log("Type:", type);
    console.log("Item ISBN:", itemIsbn);

    // 결과를 표시할 요소
    const resultElement = document.getElementById(itemIsbn);

    // 디버깅: resultElement가 null인지 확인
    if (!resultElement) {
        console.error(`Element with id "${itemIsbn}" not found.`);
        return;
    }

    // 현재 화면에 표시된 값
    let number = resultElement.innerText.replace(' 개', '').trim();
    console.log("Current Number:", number);

    // 더하기/빼기
    if (type === 'plus') {
        number = parseInt(number) + 1;
    } else if (type === 'minus') {
        number = Math.max(0, parseInt(number) - 1);
    }

    // 결과 출력
    resultElement.innerText = number + ' 개';
    console.log("Updated Number:", number);

    // fetch API를 사용하여 POST 요청 보내기
    fetch(`/api/cart/${type === 'plus' ? 'increaseItem' : 'decreaseItem'}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify({
            amount: number,
            itemIsbn: itemIsbn
        })
    })
    .then(response => {
        if (response.ok) {
            return response.json();
        }
        throw new Error('Network response was not ok.');
    })
    .then(data => {
        console.log("Response Data:", data);
        // 성공적인 응답 처리
    window.location.href = '/cart/list';
    }    )
    .catch(error => {
        console.error('Error during request:', error);
    });
}
        // Daum 우편번호 검색 기능을 위한 자바스크립트
function sample4_execDaumPostcode() {
    new daum.Postcode({
        oncomplete: function(data) {
            var roadAddr = data.roadAddress; // 도로명 주소 변수
            var extraRoadAddr = ''; // 참고 항목 변수

                        // 법정동명이 있을 경우 추가한다. (법정리는 제외)
            if(data.bname !== '' && /[동|로|가]$/g.test(data.bname)){
                extraRoadAddr += data.bname;
            }
                        // 건물명이 있고, 공동주택일 경우 추가한다.
            if(data.buildingName !== '' && data.apartment === 'Y'){
               extraRoadAddr += (extraRoadAddr !== '' ? ', ' + data.buildingName : data.buildingName);
            }
                    // 표시할 참고항목이 있을 경우, 괄호까지 추가한 최종 문자열을 만든다.
            if(extraRoadAddr !== ''){
                extraRoadAddr = ' (' + extraRoadAddr + ')';
            }

                // 우편번호와 주소 정보를 해당 필드에 넣는다.
            document.getElementById('postcode').value = data.zonecode;
            document.getElementById("roadAddress").value = roadAddr;
            document.getElementById("jibunAddress").value = data.jibunAddress;

                        // 참고항목 문자열이 있을 경우 해당 필드에 넣는다.
            if(roadAddr !== ''){
                document.getElementById("extraAddress").value = extraRoadAddr;
            } else {
                document.getElementById("extraAddress").value = '';
            }
        }
    }).open();
}

/* 회원가입 폼 길이 및 공백 체크 */

document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('signupForm').addEventListener('submit', function(event) {
        event.preventDefault(); // 기본 폼 제출 막기

        var username = document.getElementById('username').value.trim();
        var nickname = document.getElementById('nickname').value.trim();
        var password = document.getElementById('password').value.trim();
        var confirmPassword = document.getElementById('passwordConfirm').value.trim();
        var age = document.getElementById('age').value.trim();
        var postcode = document.getElementById('postcode').value.trim();
        var roadAddress = document.getElementById('roadAddress').value.trim();
        var jibunAddress = document.getElementById('jibunAddress').value.trim();
        var detailAddress = document.getElementById('detailAddress').value.trim();
        var extraAddress = document.getElementById('extraAddress').value.trim();

        var message = '';
        const regex_pwd = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#.~_-])[A-Za-z\d@$!%*?&#.~_-]{8,20}$/;

        // 유효성 검사
        if (username.length < 3 || username.length > 10) {
            message = 'Username은 3글자 이상, 10글자 이하로 입력해주세요.';
        } else if (!regex_pwd.test(password)) {
            message = '비밀번호는 8자 이상, 20자 이하, 하나 이상의 대문자, 소문자, 숫자 및 특수 문자를 포함해야 합니다.';
        } else if (password !== confirmPassword) {
            message = '비밀번호가 일치하지 않습니다.';
        }

        document.getElementById('message').textContent = message;

        // 유효성 검사가 통과되었을 때만 폼 제출
        if (message === '') {
            // 폼 제출
            event.target.submit();
        }
    });

    // 엔터 키로 다음 입력 칸으로 이동
    var inputs = document.querySelectorAll('#signupForm input, #signupForm select');
    inputs.forEach((input, index) => {
        input.addEventListener('keydown', function(event) {
            if (event.key === 'Enter') {
                event.preventDefault();
                var nextInput = inputs[index + 1];
                if (nextInput) {
                    nextInput.focus();
                } else {
                    document.querySelector('button[type="submit"]').focus();
                }
            }
        });
    });
});


// 중복체크 창을 띄운다.
document.addEventListener('DOMContentLoaded', function() {
    const checkUsernameBtn = document.getElementById('checkUsernameBtn');

    if (checkUsernameBtn) {
        checkUsernameBtn.addEventListener('click', function() {

            window.open('/check-username', '_blank', 'width=600,height=400');
        });
    }
});

//회원정보 수정
document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('edit-profile').addEventListener('submit', function(event) {
        event.preventDefault(); // 기본 폼 제출 막기

        var username = document.getElementById('username').value.trim();
        var nickname = document.getElementById('nickname').value.trim();
        var password = document.getElementById('password').value.trim();
        var confirmPassword = document.getElementById('passwordConfirm').value.trim();
        var age = document.getElementById('age').value.trim();
        var postcode = document.getElementById('postcode').value.trim();
        var roadAddress = document.getElementById('roadAddress').value.trim();
        var jibunAddress = document.getElementById('jibunAddress').value.trim();
        var detailAddress = document.getElementById('detailAddress').value.trim();
        var extraAddress = document.getElementById('extraAddress').value.trim();

        var message = '';
        const regex_pwd = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#.~_-])[A-Za-z\d@$!%*?&#.~_-]{8,20}$/;

        // 유효성 검사
        if (username.length < 3 || username.length > 10) {
            message = 'Username은 3글자 이상, 10글자 이하로 입력해주세요.';
        } else if (password && !regex_pwd.test(password)) {
            message = '비밀번호는 8자 이상, 20자 이하, 하나 이상의 대문자, 소문자, 숫자 및 특수 문자를 포함해야 합니다.';
        } else if (password !== confirmPassword) {
            message = '비밀번호가 일치하지 않습니다.';
        }

        document.getElementById('message').textContent = message;

        // 유효성 검사가 통과되었을 때만 폼 제출
        if (message === '') {
            // 폼 제출
            event.target.submit();
        }
    });

    // 엔터 키로 다음 입력 칸으로 이동
    var inputs = document.querySelectorAll('#edit-profile input, #edit-profile select');
    inputs.forEach((input, index) => {
        input.addEventListener('keydown', function(event) {
            if (event.key === 'Enter') {
                event.preventDefault();
                var nextInput = inputs[index + 1];
                if (nextInput) {
                    nextInput.focus();
                } else {
                    document.querySelector('button[type="submit"]').focus();
                }
            }
        });
    });
});


document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('editProfileForm').addEventListener('submit', function(event) {
        event.preventDefault(); // 기본 폼 제출 막기

        var formData = new FormData(event.target);

        fetch('/update-profile', {
            method: 'POST',
            body: formData,
            headers: {
                'X-Requested-With': 'XMLHttpRequest' // AJAX 요청임을 나타냄
            }
        })
        .then(response => response.json()) // 서버에서 JSON 응답을 받을 경우
        .then(data => {
            if (data.success) {
                // 업데이트가 성공하면 프로필 페이지로 이동
                window.location.href = '/profile';
            } else {
                // 업데이트 실패 시 사용자에게 알림
                document.getElementById('message').textContent = data.message;
            }
        })
        .catch(error => {
            console.error('업데이트 오류:', error);
            document.getElementById('message').textContent = '업데이트 중 오류가 발생했습니다.';
        });
    });
});

document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('editProfileForm').addEventListener('submit', function(event) {
        event.preventDefault(); // 기본 폼 제출 막기

        var password = document.getElementById('password').value.trim();
        var confirmPassword = document.getElementById('passwordConfirm').value.trim();
        var message = '';

        // 비밀번호 유효성 검사
        if (password && (password.length < 8 || password.length > 20)) {
            message = '비밀번호는 8자 이상, 20자 이하로 입력해주세요.';
        } else if (password !== confirmPassword) {
            message = '비밀번호가 일치하지 않습니다.';
        }

        document.getElementById('message').textContent = message;

        // 유효성 검사가 통과되었을 때만 폼 제출
        if (message === '') {
            var formData = new FormData(event.target);

            fetch('/update-profile', {
                method: 'POST',
                body: formData,
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    window.location.href = '/profile';
                } else {
                    document.getElementBy
