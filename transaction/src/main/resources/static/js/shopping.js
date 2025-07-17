window.onload = function () {
    var mainApp = new Vue({
        el: '#app',
        data: {
            // 用户状态
            isLoggedIn: false,
            loginDialogVisible: false,

            // 搜索和筛选
            searchKeyword: '',
            currentCategory: 1,
            priceRange: [0, 5000],
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
            products: [
                {
                    id: 1,
                    name: 'Apple iPhone 13 Pro Max 远峰蓝色 256GB',
                    price: 8999.00,
                    rating: 4.9,
                    image: 'https://imgservice.suning.cn/uimg1/b2c/image/7v5m1pDv2vGQ8hxK8pBfCQ.jpg_800w_800h_4e',
                    category: 2,
                    brand: '苹果'
                },
                {
                    id: 2,
                    name: '华为 Mate 50 Pro 昆仑破晓 512GB',
                    price: 6999.00,
                    rating: 4.8,
                    image: 'https://imgservice.suning.cn/uimg1/b2c/image/JFcF1N2g4qEwK2JjYcXrMg.jpg_800w_800h_4e',
                    category: 2,
                    brand: '华为'
                },
                {
                    id: 3,
                    name: '小米13 5G手机 12GB+256GB 白色',
                    price: 4299.00,
                    rating: 4.7,
                    image: 'https://imgservice.suning.cn/uimg1/b2c/image/1Q8J5GQcQaGg6v1gYh3JYQ.jpg_800w_800h_4e',
                    category: 2,
                    brand: '小米'
                },
                {
                    id: 4,
                    name: 'Apple MacBook Pro 14英寸 M2 Pro芯片',
                    price: 15999.00,
                    rating: 4.95,
                    image: 'https://imgservice.suning.cn/uimg1/b2c/image/1V6c1YcR2vGQ8hxK8pBfCQ.jpg_800w_800h_4e',
                    category: 3,
                    brand: '苹果'
                },
                {
                    id: 5,
                    name: '戴尔XPS 13 9315 13.4英寸轻薄笔记本',
                    price: 8999.00,
                    rating: 4.7,
                    image: 'https://imgservice.suning.cn/uimg1/b2c/image/7v5m1pDv2vGQ8hxK8pBfCQ.jpg_800w_800h_4e',
                    category: 3,
                    brand: '戴尔'
                },
                {
                    id: 6,
                    name: '索尼 PlayStation 5 游戏主机',
                    price: 3899.00,
                    rating: 4.9,
                    image: 'https://imgservice.suning.cn/uimg1/b2c/image/5N6c5YcR2vGQ8hxK8pBfCQ.jpg_800w_800h_4e',
                    category: 4,
                    brand: '索尼'
                },
                {
                    id: 7,
                    name: 'Apple AirPods Pro (第二代)',
                    price: 1899.00,
                    rating: 4.8,
                    image: 'https://imgservice.suning.cn/uimg1/b2c/image/7v5m1pDv2vGQ8hxK8pBfCQ.jpg_800w_800h_4e',
                    category: 2,
                    brand: '苹果'
                },
                {
                    id: 8,
                    name: '佳能 EOS R6 Mark II 全画幅微单相机',
                    price: 15999.00,
                    rating: 4.85,
                    image: 'https://imgservice.suning.cn/uimg1/b2c/image/1V6c1YcR2vGQ8hxK8pBfCQ.jpg_800w_800h_4e',
                    category: 4,
                    brand: '佳能'
                }
            ]
        },
        computed: {
            // 过滤商品
            filteredProducts() {
                return this.products.filter(product => {
                    // 分类过滤
                    if (this.currentCategory !== 1 && product.category !== this.currentCategory) {
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
        methods: {
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
