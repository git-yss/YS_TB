
var mainApp = new Vue({
    el: '#app', //el表示当前我们new的vue实例,要控制页面上的哪个区域
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
                window.location.href = 'index.html';
            } else {
                alert('请输入用户名和密码');
            }
        }
    }
})

