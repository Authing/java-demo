# Java SpringBoot 快速开始

本教程讲述在 Java SpringBoot 框架中集成 Authing 的用登录、检查登录状态、获取用户信息、登出等功能。

环境要求：
- SpringBoot: 2.4 及以上
- Java: jdk 1.8 及以上

在这篇教程中，我们会将 Authing 提供的 Java SDK 接入一个新建的 SpringBoot 项目，包含以下内容：

- 演示如何进行登录/注册；
- 演示如何获取用户信息；
- 演示如何和 Flask 的 Session 机制结合；
- 演示如何使用 Access Token 对接口进行登录鉴权保护；
- 演示如何对一个接口进行细粒度的权限控制保护；
- 演示如何使用 Refresh Token 换取新的 Access Token；
- 演示如何退出登录。

<br>

<img src="../images/authing-flask-demo.jpg" alt="drawing" width="400"/>


<br>


<details>
<summary><strong>如果你想运行此项目进行快速体验，点此展开</strong></summary>

在本文件所在目录下执行下面的命令安装依赖：

```bash
pip install -r requirements.txt
```

使用下面的指令启动项目（此项目默认运行在 3000 端口，如果你本地端口已被占用，可以使用 `--port` 参数指定不同的短裤）：

```bash
flask --app app run --port=3000
```

使用浏览器访问 [http://localhost:3000](http://localhost:3000) 即可。

</details>

<br>

## 在 Authing 中进行配置

你需要先在 Authing 创建一个应用。进入[**控制台**](https://console.authing.cn) > **应用**，点击右上角的「添加应用」。

<img src="../images/create-app.png" alt="drawing" width="400"/>

**认证地址**填写一个域名，作为这个应用在 Authing 的唯一标识。

<img src="../images/create-app-2.png" alt="drawing" width="400"/>

在应用列表找到你的应用，进入应用详情。在右方的「高级配置」模块中，**id_token 签名算法**选择 **RS256**，然后点击保存。

### 配置登录回调地址

当用户在 Authing 完成认证后，Authing 会将用户重定向到回调地址。必须在这里**配置回调地址白名单**，否则用户会遇到回调地址不匹配的错误信息。本教程需要用到的回调地址是 `http://localhost:5000/callback` 请在「应用配置」模块下方「URL设置」卡片的登录回调地址中粘贴此链接。

<img src="../images/set-url-1.png" alt="drawing" width="600"/>

<img src="../images/set-url.png" alt="drawing" width="400"/>

### 配置登出回调地址

当用户在 Authing 完成退出后，Authing 会将用户重定向到登出回调地址。必须在这里**配置登出回调地址白名单**，否则用户会遇到登出回调地址不匹配的错误信息。本教程需要用到的回调地址是 `http://localhost:5000` 请在「应用配置」模块下方「URL设置」卡片的登出回调地址中粘贴此链接。

<img src="../images/set-url-1.png" alt="drawing" width="400"/>

<img src="../images/set-url.png" alt="drawing" width="400"/>


### 记录应用信息

记录以下信息：

- 应用 ID
- 应用密钥
- 应用域名

<img src="../images/save-app-info.png" alt="drawing" width="400"/>

## 安装依赖

在 `requirements.txt` 中加入下面的依赖：

```
Flask==2.2.2
Flask-Session
authing
python-dotenv
```

> 其中用到了 [Authing Python SDK - authing](https://github.com/authing/authing-py-sdk)。

执行 `pip install -r requirements.txt`

## 初始化 Flask App

在这里我们给 Flask 配置了 [Session](https://flask-session.readthedocs.io/en/latest/)，出于演示的便利，我们使用 `filesystem` 这种简单的 Session 存储模式。

> 我们将相关的 html/css 文件放在了 `templates` 和 `static` 目录下。

```python
from flask import Flask, redirect, request, session, abort, render_template, send_from_directory
from flask_session import Session
from dotenv import load_dotenv

# 加载 .env 文件中的环境变量
load_dotenv()

# 初始化 Flask APP
app = Flask(__name__)

# 初始化 session
app.config["SESSION_PERMANENT"] = False
app.config["SESSION_TYPE"] = "filesystem"
Session(app)

# 定义根路由
@app.route("/")
def index():
    return render_template('index.html')
```

运行以下指令启动应用：

```
flask --app app run --port=3000
```

你应该可以看到下面的初始页面：

<img src="../images/index-not-logged-in.jpg" alt="drawing" width="400"/>


我们在这里放置了一个登录链接，目前当你点击时会提示 404 错误，不用惊慌，这属于正常情况，我们下面会开始实现登录功能。

## 初始化 Authing SDK

在这里我们把 Authing 相关的配置写到 `.env` 文件，`.env` 的格式放在了 `.env.template` 文件中，执行 `cp .env.template .env` 复制内容到 `.env` 文件，然后按照配置说明对应的配置即可。

```python
import os
from authing import AuthenticationClient

# 初始化 authing sdk 的 AuthenticationClient
authentication_client = AuthenticationClient(
    # Authing 应用 ID
    app_id=os.getenv('AUTHING_APP_ID'),

    # Authing 应用密钥
    app_secret=os.getenv('AUTHING_APP_SECRET'),

    # Authing 应用地址，如 https://example.authing.cn
    app_host=os.getenv('AUTHING_APP_HOST'),

    # 认证完成后的重定向目标 URL。可选，默认使用控制台中配置的第一个回调地址。
    redirect_uri=os.getenv('AUTHING_APP_REDIRECT_URI'),

    # 登出之后的回调地址，此地址在 Authing 控制台应用详情中的「登出回调 URL」中
    # 此参数可选，在 /logout 路由中用到的 build_logout_url 方法默认会使用此配置作为退出登录之后的回调地址
    post_logout_redirect_uri=os.getenv('AUTHING_APP_POST_LOGOUT_REDIRECT_URI'),

    # 获取 token 端点验证方式，可选值为 client_secret_post、client_secret_basic、none，默认为 client_secret_post。
    token_endpoint_auth_method=os.getenv('AUTHING_APP_TOKEN_ENDPOINT_AUTH_METHOD'),
)
```

## 实现登录/注册功能

在这里我们使用 `AuthenticationClient` 的 `build_authorize_url` 构建一个登录地址，需要注意的是：我们在 `scope` 中添加了 `offline_access`，这样我们在后面获取 `access_token` 的时候，可以得到 `refresh_token`，这样当我们的 `access_token` 过期时，可以使用 `refresh_token` 换取新的 `access_token`，从而在保证安全性的前提下，实现长期保持用户登录态无需用户再次登录的功能。

```python
@app.route("/login")
def login():
    scopes = [
        "openid", "profile",
        # 添加 offline_access 以便在 get_access_token_by_code 时返回 refresh_token
        "offline_access"
    ]
    url = authentication_client.build_authorize_url(
        scope=" ".join(scopes)
    )
    return redirect(url)
```

之后重启应用，当我们再次点击**登录**链接，你会发现我们跳转到了你的应用在 Authing 云上的登录页：


<img src="../images/authing-cloud-login-page.jpg" alt="drawing" width="400"/>


用户在此可以使用任何你的应用支持的登录/注册方式进行登录/注册，你可以[阅读此文档](https://docs.authing.co/v2/guides/app-new/create-app/)了解如何为你的应用自定义样式、配置社会化登录、配置 MFA 等。

当用户登录之后，浏览器会回调到我们在上述步骤中在 Authing 控制台配置的**登录回调 URL**，即 `http://localhost:3000/callback`：

<img src="../images/redirect_page_not_implemented.jpg" alt="drawing" width="400"/>


到目前为止，你会得到一个 `404` 的错误提示，业务我们还没有实现 `/callback` 这个路由。同时你可以看到在我们的 URL 中，有一串随机字符串 `code`，下面我们会演示如何使用这个 `code` 换取用户的 `access_token`。

## 实现换取 access_token 功能

在这里我们首先从 URL 的 query 中解析得到一次性的临时凭证 `code`，然后我们调用 AuthenticationClient 的 `get_access_token_by_code` 方法换取 `access_token`，`id_token` 和 `refresh_token`，并且将这些信息写到了 Session 中。

在这里你会接触几个术语：

- `access_token`: `access_token` 相当于**钥匙**，代表了用户访问你的业务接口或者 Authing 接口时需要提供的访问凭证。
- `id_token`: `access_token` 相当于身份证，包含了用户的一些基本个人资料。
- `refresh_token`: 当 `access_token` 过期时，可以使用 `refresh_token` 换取新的 `access_token`，从而在保证安全性的前提下，实现长期保持用户登录态无需用户再次登录的功能。

需要注意的，**用户请求你业务系统的 API 或者 Authing 的 API 时，应该使用 `access_token` 而不是 `id_token`！**

```python
@app.route("/callback")
def callback():

    # 从 query 中解析 oidc code 参数
    # 注意：一个 code 只能被消费一次
    code = request.args.get('code')

    # 使用 oidc code 换取 access_token
    code2token_resp = authentication_client.get_access_token_by_code(code)
    access_token = code2token_resp.get('access_token')
    id_token = code2token_resp.get('id_token')
    refresh_token = code2token_resp.get('refresh_token')

    # 将用户的 access_token、id_token、refresh_token 写入 session 中
    session['logged_in'] = True;
    session['access_token'] = access_token
    session['id_token'] = id_token
    session['refresh_token'] = refresh_token

    # 跳转到首页
    return redirect("/")
```

当我们再次登录，浏览器回调到 `/callback` 路由之后，我们就会进行上述的操作，并且跳转到首页：

<img src="../images/authing-flask-demo.jpg" alt="drawing" width="400"/>


接下来我们会一一实现首页中列出的那些内容。

## 实现退出登录功能

在这里，我们进行了下面几步操作：

1. 判断 Session 中是否标记了 `logged_in`，如果没有，将用户重定向到登录页面。
2. 从 Session 中获取用户的 `id_token`，我们在登出的时候，需要用到。
3. 调用 AuthenticationClient 的 `build_logout_url` 方法，获取一个登录地址，然后重定向到该地址。

```python
@app.route("/logout")
def logout():
    # 如果用户还未登录，跳转到登录地址进行登录
    logged_in = session['logged_in']
    if not logged_in:
        return redirect('/login')

    # 从 session 中获取用户的 id_token
    id_token = session['id_token']

    # 清除 session
    session.clear()

    # 构建登出地址并进行 302 跳转
    url = authentication_client.build_logout_url(
        # 用户的 id_token，用于对用户的身份进行校验
        # 当指定了 redirect_uri 参数或者在初始化 AuthenticationClient 的时候传入了 post_logout_redirect_uri 时，此参数必填。
        id_token=id_token,

        # 登出之后的回调地址，此地址在 Authing 控制台应用详情中的「登出回调 URL」中
        redirect_uri=os.getenv('AUTHING_APP_POST_LOGOUT_REDIRECT_URI')
    )
    return redirect(url)
```

当我们点击**退出登录**链接之后，我们会发现浏览器先跳转到了 Authing 的登录页面，之后再跳转到了我们之前在 Authing 中配置的**登出回调 URL**。此时，首页将显示用户未登录：

<img src="../images/index-not-logged-in.jpg" alt="drawing" width="400"/>


## 实现获取用户信息功能

由于我们在上一步将 `access_token` 写入到了 Session 中，所以我们可以在 `session` 拿出 `access_token`。之后我们调用 AuthenticationClient 的 `set_access_token` 方法设置用户登录态，当 AuthenticationClient 有了登录态之后，我们就可以调用 `get_profile` 方法获取用户资料了。

```python
@app.route("/profile")
def get_profile():
    # 从 session 中获取 access_token
    access_token = session['access_token']
    # 使用获取到的 access_token 调用 AuthenticationClient 的 set_access_token 方法设置用户登录态
    # 当 AuthenticationClient 有了登录态之后，调用 get_profile 方法获取用户资料
    authentication_client.set_access_token(access_token)
    profile_resp = authentication_client.get_profile(
        with_custom_data=True,
        with_department_ids=True,
        with_identities=True
    )
    profile = profile_resp['data']
    return profile
```

点击查看用户信息链接之后，我们就可以拿到用户的个人信息，如下图所示：

<img src="../images/flask_profile.jpg" alt="drawing" width="400"/>

## 实现一个要求用户登录才能访问的 API

在这里，我们进行了下面几步操作：

1. 判断 Session 中是否标记了 `logged_in`，如果没有，返回 403 Forbidden 错误。
2. 从 Session 中取出 `access_token`，调用 AuthenticationClient 的 `introspect_token` 方法，在线验证 `access_token` 的合法性，如果不合法，返回 403 Forbidden 错误。
3. 从 `access_token` 解析出的 `parsed_token_resp` 中，获取 `sub` 字段，也就是用户的 ID，接下来你可以基于用户的 ID 进行任何操作，例如查询此用户的订单、查询此用户的推荐商品等。

> 出于减少网络传输请求的考虑，你也可以选择在本地校验 `access_token` 的合法性（使用 `introspect_token_offline` 方法）。**采取这种方式时，需要你在应用端做好 access_token 的销毁工作，当用户主动退出登录之后，需要将 session 或者浏览器/客户端缓存的 access_token 清除。** 因为用户主动退出登录时，`access_token` 本身签名的过期时间还是没到的，只有通过在线校验才能真正判断其合法性，所以需要你在用户主动退出时务必将 session 或者浏览器/客户端缓存的 `access_token` 清除。

```python
@app.route('/api/loggedin')
def api_endpoint_check_logged_in():
    # 如果用户还未登录，跳转到登录地址进行登录
    logged_in = session['logged_in']
    if not logged_in:
        abort(403)

    # 从 session 中获取 access_token
    access_token = session['access_token']

    # 在线检验 access_token 的合法性
    parsed_token_resp = authentication_client.introspect_token(
        token=access_token
    )
    active = parsed_token_resp.get('active')

    # 如果 access_token 不合法（已过期或者已经被撤销），返回 403 Forbidden 错误
    if not active:
        abort(403)

    # 从 access_token 中获取用户的 ID
    user_id = parsed_token_resp.get('sub')

    """
    出于减少网络传输请求的考虑，你也可以选择在本地校验 access_token 的合法性。

    IMPORTANT: 
    采取这种方式时，需要你在应用端做好 access_token 的销毁工作，当用户主动退出登录之后，需要将 session 或者浏览器/客户端缓存的 access_token 清除。
    """
    # parsed_token_resp = None
    # user_id = None
    # try:
    #     # 尝试验证 access_token 的合法性
    #     parsed_token_resp = authenticationClient.introspect_token_offline(
    #         token=access_token
    #     )
    #     user_id = parsed_token_resp.get('sub')
    # except:
    #     # 验证失败，返回 403 Forbidden 错误
    #     abort(403)

    # 接下来你可以基于 user_id 进行任何操作，例如查询此用户的订单、查询此用户的推荐商品等
    # order = db.find(user_id=user_id)

    return {
       "message": 'You are allready logged in and able to get the resource you want',
       "user_id": user_id,
       "access_token": access_token,
       "parsed_token_resp": parsed_token_resp
    }
```

当我们点击**访问一个受登录保护的端点**链接之后，我们可以看到我们拿到了这个端点的数据：


<img src="../images/flask_api_logged_in.jpg" alt="drawing" width="400"/>


## 实现一个要求用户具备特定权限的 API

在这里我们简单得修改一下 `/login` 端点，我们加入一个自定义的 `scope` —— `ecs:Start`（代表了启动 ECS 服务器的权限）。

> `scope` 的含义是用户的 `access_token` 具备哪些权限项，我们通过检验 `access_token` 的 `scope`，就可以判断用户是否具备某个特定的权限。

<br>

<details>
<summary><strong>点此展开如何在 Authing 控制台中进行必要配置</strong></summary>
TODO
</details>

<br>

重启应用，退出登录之后再次点击登录，用户的 `access_token` 权限项中将会多一项 `ecs:Start`。

```python
@app.route("/login")
def login():
    scopes = [
        "openid",
        "profile",

        # 授权启动服务器的操作权限
        "ecs:Start",

        # 添加 offline_access 以便在 get_access_token_by_code 时返回 refresh_token
        "offline_access"
    ]
    url = authentication_client.build_authorize_url(
        scope=" ".join(scopes)
    )
    return redirect(url)
```

接下来继续实现一个要求用户具备特定权限的 API，我们进行了下面几步操作：

1. 判断 Session 中是否标记了 `logged_in`，如果没有，返回 403 Forbidden 错误。
2. 从 Session 中取出 `access_token`，调用 AuthenticationClient 的 `introspect_token` 方法，在线验证 `access_token` 的合法性，如果不合法，返回 403 Forbidden 错误。
3. 从 `access_token` 解析出的 `parsed_token_resp` 中，获取 `sub` 和 `scope` 字段，然后我们判断用户的 `scope` 中是否包含了 `ecs:Start` 这一项。如果不包含则返回 403 Forbidden 错误。

> 同上，出于减少网络传输请求的考虑，你也可以选择在本地校验 `access_token` 的合法性（使用 `introspect_token_offline` 方法）。


```python
@app.route('/api/check-scope')
def api_endpoint_check_scope():
    # 如果用户还未登录，跳转到登录地址进行登录
    logged_in = session['logged_in']
    if not logged_in:
        abort(403)

    # 从 session 中获取 access_token
    access_token = session['access_token']

    # 这里我们选择在线检验 access_token 的合法性，你也可以在本地校验，详情见 /api/loggedin 路由
    parsed_token_resp = authentication_client.introspect_token(
        token=access_token
    )
    active = parsed_token_resp.get('active')
    scope = parsed_token_resp.get('scope').split(' ')

    desired_scope = "ecs:Start"
    # 如果 access_token 不合法（已过期或者已经被撤销），返回 403 Forbidden 错误
    if not active or not desired_scope in scope:
        abort(403)

    # 从 access_token 中获取用户的 ID
    user_id = parsed_token_resp.get('sub')

    return {
       "message": f'Your scope contains "{desired_scope}", you are able to execute the action you want',
    }

```

当我们点击**访问一个受特定权限保护的端点**链接之后，我们可以看到我们拿到了这个端点的数据：

<img src="../images/flask_check_scope.jpg" alt="drawing" width="400"/>


你可能会问，新的 `scope` 权限项只有在用户重新登录之后才会生效，那我怎么对用户的权限进行实时校验呢？

TODO

## 实现使用 refresh_token 换取新的 access_token 功能

在上一步中，你可能会思考：如果用户的 `access_token` 快要过期了，我怎么能在不要求用户重新登录的情况下继续保持登录态？这个时候，`refresh_token` 就派上用场了。

当用户的 `access_token` 过期时，可以使用 `refresh_token` 换取新的 `access_token`，从而在保证安全性的前提下，实现长期保持用户登录态无需用户再次登录的功能。

由于我们在之前的步骤中将 `refresh_token` 写入到了 Session 中，所以我们可以在 `session` 拿出 `refresh_token`。

之后我们调用 AuthenticationClient 的 `get_new_access_token_by_refresh_token` 方法换取新的 `access_token`, `id_token` 和 `refresh_token`，并将这些信息更新到 Session 中。

```python
@app.route("/refresh-token")
def refresh_token():
    # 从 session 中获取 refresh_token
    refresh_token = session['refresh_token']

    # 使用 refresh_token 获取新的 access_token, id_token 和 refresh_token
    get_refresh_token_resp = authentication_client.get_new_access_token_by_refresh_token(
        refresh_token=refresh_token
    )
    access_token = get_refresh_token_resp.get('access_token')
    id_token = get_refresh_token_resp.get('id_token')
    refresh_token = get_refresh_token_resp.get('refresh_token')

    # 将新的 access_token、id_token、refresh_token 写入 session 中
    session['logged_in'] = True;
    session['access_token'] = access_token
    session['id_token'] = id_token
    session['refresh_token'] = refresh_token

    return get_refresh_token_resp
```

点击**使用 Refresh Token 换取新的 Access Token** 链接之后，我们可以看到获取到的新数据，如下图所示：

<img src="../images/flask-refresh-token.jpg" alt="drawing" width="400"/>


## 接下来

到目前为止，我们的所有功能都已经全部实现了，接下来你可以了解更多：

1. 在这个项目中，我们使用的是 Authing 在线的托管登录页，我们也提供 [React, Vue, Angular 以及原生 JS 的登录组件 —— Guard](https://docs.authing.co/v2/reference/guard/v2/)，你可以在你的系统中集成该组件，功能和托管登录页完全一致。你也可以选择自建登录页面，我们 Python SDK 内置了发送短信、注册、手机号验证码登录等方法，可以让你在自建的登录页面中实现 Authing 登录页/组件一样的功能。
2. 你可以[阅读此文档](https://docs.authing.co/v2/guides/app-new/create-app/)了解如何为你的应用自定义样式、配置社会化登录、配置 MFA 等。
3. 如果你有多个系统，需要在不同的站点中实现单点登录效果，可以阅读文档[如何实现单点登录 SSO](https://docs.authing.co/v2/reference/sdk-for-sso-spa.html)。


## 获取帮助

如果对于此 Demo 有任何疑问，可访问[官方论坛](https://forum.authing.cn/). 此仓库的 issue 仅用于上报 Bug 和提交新功能特性。