function removeUser() {

    // CSRF 토큰과 헤더 이름 가져오기
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;

    console.log("Remove User");

    fetch('/delete-account', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        }
    })
    .then(response => {
        if (response.ok) {
            return response.json();
        }
        throw new Error('Error');
    })
    .then(data => {
        console.log("Response Data:", data);
        if (data.message) {
            window.location.href = '/logout';
        } else if (data.error) {
            console.error('Error from server:', data.error);
            alert('Failed to remove user: ' + data.error);
        }
    })
    .catch(error => {
        console.error('Error during request:', error);
        alert('Failed to remove user due to a network error.');
    });
}
