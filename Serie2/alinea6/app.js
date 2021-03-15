const express = require('express')
const app = express()
const crypto = require('crypto')
const db = require('./data')()
const request = require('request')
const fs = require('fs')


const port = 3001;

//Get credentials
var data = fs.readFileSync("credentials.json", 'utf8')
const CLIENT_ID = JSON.parse(data).web.client_id
const CLIENT_SECRET = JSON.parse(data).web.client_secret
const CLIENT_ID_GITHUB = JSON.parse(data).github.client_id
const CLIENT_SECRET_GITHUB = JSON.parse(data).github.client_secret
const CALLBACK = 'callback'
const CALLBACK_GITHUB = 'github/callback'


//Additional functions
const getAppCookies = (req) => {
    // extract the raw cookies from the request headers

    const rawCookies = req.headers.cookie.split('; ');
    const parsedCookies = {};
    rawCookies.forEach(rawCookie => {
        const parsedCookie = rawCookie.split('=');
        parsedCookies[parsedCookie[0]] = parsedCookie[1];
    });
    return parsedCookies;
}

const verifycookie = (req, resp) => {
    const hmac = crypto.createHmac('sha256', 'changeit');
    if (req.headers.cookie) {
        const t1 = getAppCookies(req, resp)['T'];
        const id = getAppCookies(req, resp)['HCookie'];
        const t2 = hmac.update(id).digest('hex');
        if (t1 == t2) {
            if (db.verifyId(id)) {
                return true
            } else {
                return false
            }
        } else {
            return false
        }
    } else {
        return false
    }
}

//Start Routes
app.get('/', (req, resp) => {
    if (verifycookie(req, resp)) {
        const id = getAppCookies(req, resp)['HCookie'];
        if (db.verifyGit(id)) {
            resp.statusCode = 200;
            resp.send(`<form action="/listRepositories"> 
                    <label for="user">Insert Github User:</label><br>
                    <input type="text" id="user" name="user"><br>
                    <label for="repo">Insert Github Repo:</label><br>
                    <input type="text" id="repo" name="repo"><br><br>
                    <input type="submit" value="Submit">
                </form> `)
        } else {
            resp.statusCode = 200;
            resp.send('<a href=/loginGithub>Use Github Account</a>')
        }
    } else {
        resp.statusCode = 200;
        resp.send('<a href=/loginGoogle>Use Google Account</a>')
    }
})

app.get('/loginGoogle', (req, resp) => {
    resp.redirect(302, // authorization endpoint
        'https://accounts.google.com/o/oauth2/v2/auth?'

        // client id
        +
        'client_id=' + CLIENT_ID + '&'

        // scope "openid email"
        +
        //$20 
        'scope=openid%20email%20https://www.googleapis.com/auth/tasks%20https://www.googleapis.com/auth/calendar%20https://www.googleapis.com/auth/calendar.events&'

        // parameter state should bind the user's session to a request/response
        +
        'state=some-id-based-on-user-session&'

        // responde_type for "authorization code grant"
        +
        'response_type=code&'

        // redirect uri used to register RP
        +
        'redirect_uri=http://localhost:3001/' + CALLBACK)
})

app.get('/callback', (req, resp) => {
    const hmac = crypto.createHmac('sha256', 'changeit')
    resp.statusCode = 200
    const id = Math.random().toString();
    const h = hmac.update(id).digest('hex')
    //console.log(h)
    resp.setHeader('Set-Cookie', ['HCookie=' + id, 'T=' + h])
    resp.cookie('HCookie2', id + ':' + h, {
        expires: new Date(Date.now() + 900000),
        httpOnly: true
    });

    request.post({
        url: 'https://www.googleapis.com/oauth2/v3/token',
        // body parameters
        form: {
            code: req.query.code,
            client_id: CLIENT_ID,
            client_secret: CLIENT_SECRET,
            redirect_uri: 'http://localhost:3001/' + CALLBACK,
            grant_type: 'authorization_code'
        }
    }, function (err, httpResponse, body) {
        //
        // TODO: check err and httpresponse
        //
        var json_response = JSON.parse(body);
        var session = {
            access_token: json_response.access_token,
            id_token: json_response.id_token
        }

        db.createInDb(id, session)
        //db.printDB()
    })
    resp.send('<a href=/loginGithub>Use Github Account</a>')
})

app.get('/loginGithub', (req, resp) => {
    if (verifycookie(req, resp)) {
        resp.redirect(302,
            'https://github.com/login/oauth/authorize?' +
            'client_id=' + CLIENT_ID_GITHUB +
            '&scope=repo%20admin:script' +
            '&redirect_uri=http://localhost:3001/' + CALLBACK_GITHUB)
    } else {
        resp.statusCode = 401;
        resp.send('<a href=/loginGoogle>Use Google Account</a>')
    }
})



app.get('/github/callback', (req, resp) => {
    request.post({
        url: 'https://github.com/login/oauth/access_token',
        // body parameters
        headers: {
            "Accept": "application/json"
        },
        form: {
            code: req.query.code,
            client_id: CLIENT_ID_GITHUB,
            client_secret: CLIENT_SECRET_GITHUB,
            redirect_uri: 'http://localhost:3001/' + CALLBACK_GITHUB,
            grant_type: 'authorization_code'
        }

    }, function (err, httpResponse, body) {
        //
        // TODO: check err and httpresponse
        //
        var json_response = JSON.parse(body);
        const id = getAppCookies(req, resp)['HCookie'];
        db.addGithubAccessToken(id, json_response.access_token)
        //db.printDB()
    })
    resp.send(`<form action="/listRepositories"> 
                    <label for="user">Insert Github User:</label><br>
                    <input type="text" id="user" name="user"><br>
                    <label for="repo">Insert Github Repo:</label><br>
                    <input type="text" id="repo" name="repo"><br><br>
                    <input type="submit" value="Submit">
                </form> `)
})



app.get('/listRepositories', (req, resp) => {
    if (verifycookie(req, resp)) {
        const id = getAppCookies(req, resp)['HCookie'];
        if (db.verifyGit(id)) {
            request.get({
                url: `https://api.github.com/repos/${req.query.user}/${req.query.repo}/milestones`,
                headers: {
                    "Accept": "application/vnd.github.v3+json",
                    "User-Agent": `${req.query.user}`,
                    "Authorization": "token " + db.getGitAccessToken(id)
                }
            }, function (err, httpResponse, body) {
                //
                // TODO: check err and httpresponse
                //
                var json_response = JSON.parse(body)

                resp.header('content-type', 'text/html')
                json_response.forEach(element =>
                    resp.write(`<form action="/createCalendarEvent"> 
                                    <label for="login">Login:</label>
                                    <input type="text" id="login" name="login" value= "${element.creator.login}" readonly><br>
                                    <label for="title">Title:</label>
                                    <input type="text" id="title" name="title" value= "${element.title}" readonly><br>
                                    <label for="description">Description:</label>
                                    <input type="text" id="description" name="description" value= "${element.description}" readonly><br>
                                    <label for="start">Start_Date:</label>
                                    <input type="text" id="start" name="start" value= "${element.created_at.split("T")[0]}" readonly><br>
                                    <label for="final">Final_Date:</label>
                                    <input type="text" id="final" name="final" value= "${element.due_on.split("T")[0]}" readonly><p>
                                    <input type="submit" value="Create Event">
                                </form> `))
                resp.send()
            })
        } else {
            resp.statusCode = 200;
            resp.send('<a href=/loginGithub>Use Github Account</a>')
        }
    } else {
        resp.statusCode = 401;
        resp.send('<a href=/loginGoogle>Use Google Account</a>')
    }
})

app.get('/createCalendarEvent', (req, resp) => {

    if (verifycookie(req, resp)) {
        const id = getAppCookies(req, resp)['HCookie'];
        if (db.verifyGit(id)) {
            request.post({
                url: `https://www.googleapis.com/calendar/v3/calendars/primary/events`,
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + db.getAccessToken(id)
                },
                body: JSON.stringify({
                    "start": {
                        "date": req.query.start
                    },
                    "end": {
                        "date": req.query.final
                    },
                    "summary": req.query.title,
                    "description": req.query.description
                })
            }, function (err, httpResponse, body) {
                //
                // TODO: check err and httpresponse
                //
                var json_response = JSON.parse(body)
                const obj = {
                    "Id": json_response.id,
                    "Status": json_response.status,
                    "Summary": json_response.summary,
                    "Description": json_response.description,
                }
                resp.send(obj)
            })
        } else {
            resp.statusCode = 200;
            resp.send('<a href=/loginGithub>Use Github Account</a>')
        }
    } else {
        resp.statusCode = 401;
        resp.send('<a href=/loginGoogle>Use Google Account</a>')
    }
})

app.listen(port, (err) => {
    if (err) {
        return console.log('Something bad happened', err)
    }
    console.log(`Server listening on ${port}`)
})