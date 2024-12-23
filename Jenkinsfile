pipeline {
    agent any

    environment {
        DOCKER_IMAGE_NAME = "rimsdk/jenkins-app"  // Votre username Docker Hub
        DOCKER_IMAGE_TAG = "latest"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                script {
                    // Utiliser Docker pour construire l'application avec Maven
                    docker.build("${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}", "-f Dockerfile .")
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    // Lancer les tests Maven dans le conteneur Docker
                    docker.image("${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}").inside {
                        sh 'mvn test'
                    }
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Create Docker Image') {
            steps {
                script {
                    // Utiliser Dockerfile pour créer l'image finale
                    docker.build("${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}")
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                script {
                    // Utilisation de vos credentials existants avec le nom "Rim Sadki"
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-credentials',  // Votre ID existant
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )]) {
                        sh """
                            echo \$DOCKER_PASS | docker login -u \$DOCKER_USER --password-stdin
                            docker push ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}
                        """
                    }
                }
            }
        }
    }

    post {
        always {
            cleanWs()  // Nettoyage de l'espace de travail après l'exécution du pipeline
        }
    }
}
