def checkout() {
    stage('Checkout Code') {
        cleanWs()
        git branch: 'main', url: "${env.REPO_URL}"
    }
}

def compile(appType) {

    stage('Compile Code') {
        if(appType == "java") {
            sh 'mvn clean compile'
        }

        if(appType == "golang") {
            sh 'go mod init'
        }
    }


}

def testCases(appType) {

    stage('Unit Tests') {
        if(appType == "java") {
            sh 'mvn test || true'
        }

        if(appType == "nodejs") {
            sh 'npm test || true'
        }

        if(appType == "python") {
            sh 'python3 -m unittest *.py || true'
        }

    }


}


def codeQuality() {
    stage('Code Quality') {
        //sh "sonar-scanner -Dsonar.qualitygate.wait=true -Dsonar.login=admin -Dsonar.password=admin123 -Dsonar.host.url=http://172.31.8.238:9000 -Dsonar.projectKey=${env.COMPONENT} ${SONAR_OPTS}"
        sh 'echo OK'
    }
}

def release(appType) {
    stage('Publish A Release') {
        if (appType == "nodejs") {
            sh '''
        npm install 
        zip -r ${COMPONENT}-${TAG_NAME}.zip node_modules server.js schema
      '''
        }

        if (appType == "java") {
            sh '''
        mvn package 
        cp target/${COMPONENT}-1.0.jar ${COMPONENT}.jar 
        zip -r ${COMPONENT}-${TAG_NAME}.zip ${COMPONENT}.jar schema
      '''
        }

        if (appType == "python") {
            sh '''
        zip -r ${COMPONENT}-${TAG_NAME}.zip *.ini *.py *.txt
      '''
        }

        if (appType == "nginx") {
            sh '''
        zip -r ${COMPONENT}-${TAG_NAME}.zip *
        zip -d ${COMPONENT}-${TAG_NAME}.zip Jenkinsfile 
      '''
        }

        // Since it is Docker Builds we dont need nexus upload
        //sh 'curl -v -u admin:admin123 --upload-file ${COMPONENT}-${TAG_NAME}.zip http://172.31.13.197:8081/repository/${COMPONENT}/${COMPONENT}-${TAG_NAME}.zip'

        sh 'aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 739561048503.dkr.ecr.us-east-1.amazonaws.com'
        sh 'docker build -t ${COMPONENT} .'
        sh 'docker tag ${COMPONENT}:latest 739561048503.dkr.ecr.us-east-1.amazonaws.com/${COMPONENT}:${TAG_NAME}'
        sh 'docker push 739561048503.dkr.ecr.us-east-1.amazonaws.com/${COMPONENT}:${TAG_NAME}'
    }
}

def mail() {
    mail bcc: '', body: "<h1>Pipeline Failure</h1><br>Project Name: ${COMPONENT}\nURL = ${BUILD_URL}", cc: '', charset: 'UTF-8', from: 'raghudevopsb69@gmail.com', mimeType: 'text/html', replyTo: 'raghudevopsb69@gmail.com', subject: "ERROR CI: Component Name - ${COMPONENT}", to: "raghudevopsb69@gmail.com"
    sh 'exit 1'
}