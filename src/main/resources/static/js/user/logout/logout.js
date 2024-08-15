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
