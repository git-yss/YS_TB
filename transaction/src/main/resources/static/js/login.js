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
                sessionStorage.setItem('loginUser', this.username)
                fetchData(contextPath+'/login',params,function () {
                    window.location.href = 'shopping.html';
                },this)
            } else {
                alert('请输入用户名和密码');
            }
        }
    }
})
};

