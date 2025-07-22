
 function fetchData(url,params,callback, context) {
    fetch(url,{
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(params)
    }).then(response =>  response.json()) // 解析JSON格式的响应体
        .then(data => {
            console.log(data); // 处理响应数据
            if(data.status==200){
                window.location.href = 'shopping.html';
                callback && callback(data);
            }else{
                if (context && context.$message && context.$message.error) {
                    context.$message.error(data.msg || '请求失败');
                } else {
                    alert(data.msg || '请求失败');
                }
            }
        })
        .catch(error => {
            console.error('Error fetching data:', error); // 处理错误
        });
}