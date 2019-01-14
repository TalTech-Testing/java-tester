node('build_tester') {
  try {
    stage("Checkout repositories") {
      checkout scm
    }

    stage("Build docker container") {
      sh(returnStdout: false, script: './gradlew clean docker -PareteEnv=' + env.BRANCH_NAME + '-Dorg.gradle.java.home=/usr/lib/jvm/jdk-11.0.1/')
    }

    stage("Deployment") {
      name = 'studenttester-' + env.BRANCH_NAME + '/2.0'
      sh(returnStdout: false, script: 'docker save -o studenttester.tar ' + name)
      withCredentials([[$class: 'FileBinding', credentialsId: '98ba0434-af5e-4432-8fa1-14b2b2633208', variable: 'SSHKEYFILE']]) {
        sh('eval `ssh-agent -s`;'
         + 'ssh-agent bash -c \'ssh-add "' + env.SSHKEYFILE + '";'
         + 'scp studenttester.tar jenkins@193.40.252.119: '
         + '&& ssh jenkins@193.40.252.119 "docker load -i studenttester.tar"')
      }
    }
  } catch(err) {
    currentBuild.result = 'FAILURE'
    throw err
  }

}
