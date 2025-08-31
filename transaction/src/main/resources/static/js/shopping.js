window.onload = function () {
    var mainApp = new Vue({
        el: '#app',
        data: {
            // 用户状态
            isLoggedIn: true,
            loginDialogVisible: true,
            totalProducts: 100,    // 总商品数量
            currentPage: 1,      // 当前页码
            pageSize: 20,        // 每页显示数量
            // 搜索和筛选
            searchKeyword: '',
            currentCategory: 1,
            priceRange: [0, 50000],
            selectedBrands: [],

            // 购物车
            cartItemCount: 0,
            showCart: false,

            // 分类数据
            categories: [
                { id: 1, name: '全部商品' },
                { id: 2, name: '手机数码' },
                { id: 3, name: '电脑办公' },
                { id: 4, name: '家用电器' },
                { id: 5, name: '服装鞋帽' },
                { id: 6, name: '美妆护肤' },
                { id: 7, name: '食品生鲜' },
                { id: 8, name: '运动户外' }
            ],

            // 品牌数据
            brands: ['苹果', '华为', '小米', '三星', 'OPPO', 'VIVO', '联想', '戴尔'],

            // 商品数据
            products: []
        },
        computed: {
            // 过滤商品
            filteredProducts() {
                return this.products.filter(product => {
                    // 分类过滤
                    if (this.currentCategory !== 1 && product.category != this.currentCategory) {
                        return false;
                    }

                    // 价格过滤
                    if (product.price < this.priceRange[0] || product.price > this.priceRange[1]) {
                        return false;
                    }

                    // 品牌过滤
                    if (this.selectedBrands.length > 0 && !this.selectedBrands.includes(product.brand)) {
                        return false;
                    }

                    // 关键词搜索
                    if (this.searchKeyword && !product.name.toLowerCase().includes(this.searchKeyword.toLowerCase())) {
                        return false;
                    }

                    return true;
                });
            }
        },
        mounted() {
            this.queryAllGoods();
        },
        methods: {
            queryAllGoods(page = 1){
                let _this =this
                let params = {
                    current: page,
                    size: this.pageSize
                };
                fetchData(contextPath+'/queryAllGoodsPage',params,function (data) {
                    _this.products = data.data.records;
                    _this.totalProducts = data.data.total;
                    _this.currentPage = data.data.current;
                    _this.filteredProducts = data.data.records;
                },this)
            },
            // 分页切换
            handlePageChange(page) {
                this.currentPage = page;
                this.queryAllGoods(page);
            },
            // 添加到购物车
            addToCart(product) {
                this.cartItemCount++;
                this.$message({
                    message: `已添加 ${product.name} 到购物车`,
                    type: 'success',
                    duration: 1500
                });
            },
            // 添加到收藏
            addToWishlist(product) {
                this.$message({
                    message: `已收藏 ${product.name}`,
                    type: 'success',
                    duration: 1500
                });
            },

            // 登录
            login() {
                this.isLoggedIn = true;
                this.loginDialogVisible = false;
                this.$message.success('登录成功！');
            },

            // 退出登录
            logout() {
                this.isLoggedIn = false;
                this.$message('您已退出登录');
            }
        }
    });
};
