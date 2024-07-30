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

function galarySubmit() {
	var chkbxs = document.querySelectorAll('input[type="checkbox"]');
	for(var i=0;i<chkbxs.length;i++) {
		var thumbnailEl = document.createElement('input');
		thumbnailEl.type = 'hidden';
		thumbnailEl.name = 'thumbnail';
		thumbnailEl.value = chkbxs[i].checked ? '1' : '0';
		chkbxs[i].parentElement.appendChild(thumbnailEl);
	}
	document.galaryForm.submit();
}

//      버튼 추가 기능
        document.addEventListener('DOMContentLoaded', (event) => {
            const fileInputsContainer = document.getElementById('fileInputsContainer');

            fileInputsContainer.addEventListener('change', function(event) {
                if (event.target && event.target.classList.contains('file-input')) {
                    addFileInput();
                }
            });
//
            function addFileInput() {
                // Check if the last file input is empty, don't add new input if it is
                const fileInputs = document.querySelectorAll('.file-input');
                if (fileInputs[fileInputs.length - 1].files.length === 0) {
                    return;
                }

                // Create a new file input element
                const newFileInput = document.createElement('input');
                const newId = `imageFiles${fileInputs.length}`;
                newFileInput.type = 'file';
                newFileInput.name = 'imageFiles';
                newFileInput.className = 'file-input';
                newFileInput.id = newId;
                newFileInput.multiple = true;

                // Create a new label for the file input
                const newLabel = document.createElement('label');
                newLabel.htmlFor = newId;
                newLabel.textContent = '파일 업로드:';

                // Append the new label and file input to the container
                fileInputsContainer.appendChild(newLabel);
                fileInputsContainer.appendChild(newFileInput);
            }

            // Initially add one file input if there is only one present and empty
            if (document.querySelectorAll('.file-input').length === 1) {
                addFileInput();
            }
        });

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
