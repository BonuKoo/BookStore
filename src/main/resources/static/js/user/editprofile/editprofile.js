//회원정보 수정
/*
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
*/

document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('editProfileForm').addEventListener('submit', function(event) {
        event.preventDefault(); // 기본 폼 제출 막기

        var password = document.getElementById('password').value.trim();
        var confirmPassword = document.getElementById('passwordConfirm').value.trim();
        var message = '';

        // 비밀번호 검증식
        const regex_pwd = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#.~_-])[A-Za-z\d@$!%*?&#.~_-]{8,20}$/;


if (!regex_pwd.test(password)) {
            message = '비밀번호는 8자 이상, 20자 이하로 입력해야 하며, 하나 이상의 대문자, 소문자, 숫자 및 특수 문자를 포함해야 합니다.';
        } else if (password !== confirmPassword) {
            message = '비밀번호와 확인 비밀번호가 일치하지 않습니다.';
        }

        if (message) {
            document.getElementById('message').textContent = message;
            document.getElementById('message').style.color = 'red';
        } else {
            document.getElementById('message').textContent = '';
            // 유효성 검사가 통과되었을 때만 폼 제출
            submitForm();
        }
    });
});

function submitForm() {
    var formData = new FormData(document.getElementById('editProfileForm'));

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
            document.getElementById('message').textContent = data.message;
        }
    })
    .catch(error => {
        console.error('업데이트 오류:', error);
        document.getElementById('message').textContent = '업데이트 중 오류가 발생했습니다.';
    });
}

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

