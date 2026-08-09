pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials')
        IMAGE_NAME = "shivaniavani/notepad-app"
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        PROJECT_DIR = "/workspace"
    }

    stages {

        stage('Verify Workspace') {
            steps {
                sh "ls -la ${PROJECT_DIR}"
            }
        }

        stage('Build with Maven') {
            steps {
                // Run Maven with an explicit -f pointing at the mounted
                // project's pom.xml, instead of changing Jenkins' own
                // working directory into the mount itself. This avoids
                // Jenkins needing to create its internal @tmp control
                // directory inside the mounted volume, where it lacks
                // permission to do so.
                sh "mvn -f ${PROJECT_DIR}/pom.xml clean package -DskipTests"
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    dockerImage = docker.build("${IMAGE_NAME}:${IMAGE_TAG}", "${PROJECT_DIR}")
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                script {
                    docker.withRegistry('https://registry.hub.docker.com', 'dockerhub-credentials') {
                        dockerImage.push("${IMAGE_TAG}")
                        dockerImage.push("latest")
                    }
                }
            }
        }
    }

    post {
        success {
            echo "Build and push succeeded: ${IMAGE_NAME}:${IMAGE_TAG}"
        }
        failure {
            echo "Build failed — check the stage logs above."
        }
        always {
            sh "docker rmi ${IMAGE_NAME}:${IMAGE_TAG} || true"
        }
    }
}