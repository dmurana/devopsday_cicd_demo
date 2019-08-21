pipeline {
  environment {
    registry = '<docker_hub_username>/<repository>'
    // dockerhub credentials needs to be created previously on Jenkins
    registryCredential = 'dockerhub'
    dockerImage = ''
  }
  agent any
  stages {
    stage('Cloning Git') {
      steps {
      // Clone this GitHub repo
        git 'https://github.com/dmurana/devopsday_cicd_demo.git'
      }
    }
    stage('Building image') {
      steps{
        script {
          dockerImage = docker.build registry
        }
      }
    }
    stage('Deploy Image') {
      steps{
        script {
          docker.withRegistry( '', registryCredential ) {
            dockerImage.push()
          }
        }
      }
    }
    stage('Remove Unused docker image') {
      steps{
        sh "docker rmi $registry"
      }
    }

    stage('Deploy to Cloud PKS'){
      steps{
        sh("vke account login -t <Organization ID> -r <OAuth refresh token>")
        sh("vke cluster auth setup <cluster>")
        sh("kubectl apply -f /home/jenkins/devopsdaymvd-deployment.yaml")
        sh("kubectl apply -f /home/jenkins/devopsdaymvd-service.yaml")
        sh("kubectl apply -f /home/jenkins/devopsdaymvd-ingress.yaml")
        sh("kubectl rollout restart deployment/devopsdaymvd-deploy")
      }
    }

  }
}
