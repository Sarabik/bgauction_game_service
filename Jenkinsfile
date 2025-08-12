pipeline {
  agent any
  tools { jdk 'JDK-17' }
  options {
      skipDefaultCheckout(true)
      timestamps()
      ansiColor('xterm')
    }
  environment {
    SONARQUBE_SERVER = 'SonarQube'
  }
  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Build & Test') {
      steps {
        sh 'chmod +x gradlew'
        sh './gradlew clean test jacocoTestReport'
      }
      post {
        always {
          junit 'build/test-results/test/*.xml'
        }
      }
    }

    stage('SonarQube Analysis') {
      steps {
        withSonarQubeEnv("${SONARQUBE_SERVER}") {
          sh './gradlew sonar -Dsonar.host.url=$SONAR_HOST_URL -Dsonar.login=$SONAR_AUTH_TOKEN'
        }
      }
    }

    stage('Quality Gate') {
      steps {
        timeout(time: 5, unit: 'MINUTES') {
          script {
            def qg = waitForQualityGate()
            if (qg.status != 'OK') {
              error "Quality Gate failed: ${qg.status}"
            }
          }
        }
      }
    }

    stage('Package') {
      when { branch 'main' }
      steps {
        sh './gradlew -x test build'
      }
    }
  }
}
