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

document.addEventListener('DOMContentLoaded', function() {
    const checkUsernameBtn = document.getElementById('checkUsernameBtn');
    const tooltip = document.getElementById('tooltip');

    // 버튼에 마우스를 올렸을 때
    checkUsernameBtn.addEventListener('mouseenter', function() {
        tooltip.style.opacity = '1';
        const rect = checkUsernameBtn.getBoundingClientRect();
        tooltip.style.top = `${rect.bottom + window.scrollY + 5}px`;
        tooltip.style.left = `${rect.left + (checkUsernameBtn.offsetWidth / 2) - (tooltip.offsetWidth / 2)}px`;
    });

    // 버튼에서 마우스를 뗐을 때
    checkUsernameBtn.addEventListener('mouseleave', function() {
        tooltip.style.opacity = '0';
    });
});

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
