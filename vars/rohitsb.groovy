def call(String repoUrl,String gitu,String sonarname,String sonarid,String branch) {
def choice=[]
pipeline {
    agent any
    tools {
        maven 'My_Maven'
    }
    stages {
        stage('Git Checkout') {
            steps {
                git branch: "${branch}",
                credentialsId: "${gitu}",
                url: "${repoUrl}"
            }
        }   
        stage('Compile') {
            steps {
                sh 'mvn clean install'
            }
        }
        stage('Package') {
            steps {
                sh 'mvn package'
            }
        }
        stage('Unit Test') {
            steps {
                sh 'mvn test'
            }
        }
        stage("build & SonarQube analysis") {
            steps {
             withSonarQubeEnv(credentialsId: 'cloud', installationName: 'MySonar'){
             sh 'mvn verify sonar:sonar -Dsonar.projectKey=testrepo1 -Dsonar.login=4c4e9b9c168315a700a1b32a90e04f61931a75ec -Dsonar.host.url=https://sonarcloud.io/'
             sh 'sleep 60'
           }
         }
       }
        stage("Quality Gate") {
             steps {
               timeout(time: 1, unit: 'HOURS') {
                 waitForQualityGate abortPipeline: true
           }
         }
       }
        stage("renaming-the-jar-and-backing-up-the-jar") {
            steps {
             sh 'sudo mkdir -p /opt/backup'
             sh 'sudo mv target/my-app-1.0-SNAPSHOT.jar target/1.0.${BUILD_NUMBER}.jar'
             sh 'sudo cp target/1.0.${BUILD_NUMBER}.jar /opt/backup/'
         }
       }
         stage('Deploy To Environment') {
                steps {
                 sh 'echo Deployment Done'
               }
            }
        }
    }
}
