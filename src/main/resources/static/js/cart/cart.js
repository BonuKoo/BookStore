function removeItem(itemIsbn) {
    // CSRF 토큰과 헤더 이름 가져오기
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;

    // 디버깅을 위해 변수 값 출력
    console.log("Remove item with ISBN:", itemIsbn);

    // fetch API를 사용하여 POST 요청 보내기
    fetch('/api/cart/removeItem', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify({
            itemIsbn: itemIsbn
        })
    })
    .then(response => {
        if (response.ok) {
            return response.json();
        }
        // 응답이 성공적이지 않으면 에러 발생
        throw new Error('Network response was not ok.');
    })
    .then(data => {
        console.log("Response Data:", data);
        if (data.message) {
            // 성공적인 응답 처리: 페이지 새로고침 또는 장바구니 업데이트
            window.location.href = '/cart/list';
        } else if (data.error) {
            // 에러 메시지 처리
            console.error('Error from server:', data.error);
            alert('Failed to remove item: ' + data.error);
        }
    })
    .catch(error => {
        // 요청 도중 발생한 에러 처리
        console.error('Error during request:', error);
        alert('Failed to remove item due to a network error.');
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
