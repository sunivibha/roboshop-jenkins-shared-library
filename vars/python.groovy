def call() {
    if (!env.SONAR_OPTS) {
        env.SONAR_OPTS = ""
    }
    node {
        common.checkout()
        common.codeQuality()
        common.testCases("python")
        if(env.TAG_NAME ==~ ".*") {
            common.release("python")
        }
    }
}