pipeline {
  agent any
  stages {
    stage('构建代码') {
      steps {
        sh 'mvn package -Dmaven.test.skip=true'
      }
    }
  }
}