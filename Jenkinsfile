pipeline {
    agent any

    environment {
        DOCKER_IMAGE_NAME = "rimsdk/jenkins-app" // Nom de l'image Docker
        DOCKER_IMAGE_TAG = "latest" // Tag de l'image
    }

    stages {
        stage('Checkout') {
            steps {
                // Récupération du code source depuis le dépôt
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                script {
                    // Utiliser une image Maven pour construire et tester l'application
                    docker.image('maven:3.8.6-jdk-11').inside {
                        sh 'mvn clean package' // Build et package du projet
                        sh 'mvn test' // Exécution des tests unitaires
                    }
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml' // Publier les résultats des tests
                }
            }
        }

        stage('Create Docker Image') {
            steps {
                script {
                    // Construire l'image Docker finale
                    docker.build("${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}")
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                script {
                    // Pousser l'image Docker sur Docker Hub
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-credentials', // Votre ID configuré dans Jenkins
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
            cleanWs() // Nettoyer l'espace de travail
        }
    }
}
