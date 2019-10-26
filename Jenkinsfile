pipeline {
  agent any
  stages {
    stage('error') {
      steps {
        sh 'mvn package -Dmaven.test.skip=true'
      }
    }
  }
}