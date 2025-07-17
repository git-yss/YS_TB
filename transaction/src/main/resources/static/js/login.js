window.onload = function () {
let contextPath = location.origin;
var mainApp = new Vue({
    el: '#login',
    data: {
        username: '',
        password: ''
    },

    created() {

    },
    mounted() {

    },

    methods: {
        login() {
            if (this.username && this.password) {
                let params = {
                    username: this.username,
                    password: this.password
                };
                fetch(contextPath+'/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(params)
                }).then(res => res.json())
                    .then(data => {
                        window.location.href = 'shopping.html';
                    });

            } else {
                alert('请输入用户名和密码');
            }
        }
    }
})
};

