module.exports = () => {
    const DB = []

    return {
        DB: DB,
        createInDb: createInDb,
        printDB: printDB,
        verifyId: verifyId,
        addGithubAccessToken: addGithubAccessToken,
        verifyGit: verifyGit,
        getGitAccessToken: getGitAccessToken,
        getAccessToken: getAccessToken
    }

    function createInDb(id, session) {
        const object = {
            id: id,
            session: session,
            github: null
        }

        DB.push(object)
    }

    function printDB() {
        console.log(DB)
    }

    function verifyId(id) {
        return DB.some(element => element.id === id)
    }

    function getAccessToken(id) {
        return DB.find(element => element.id === id).session.access_token
    }


    function verifyGit(id) {
        if (DB.find(element => element.id === id).github) {
            return true;
        }
        return false;
    }

    function addGithubAccessToken(id, github) {
        return DB.find(element => element.id === id).github = github
    }

    function getGitAccessToken(id) {
        return DB.find(element => element.id === id).github
    }
}