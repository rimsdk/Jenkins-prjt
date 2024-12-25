pipeline {
    agent {
        docker {
            image 'docker:latest'
            args '-v /var/run/docker.sock:/var/run/docker.sock'
        }
    }

    environment {
        DOCKER_IMAGE = "rimsdk/banking-app"
        DOCKER_TAG = "latest"
    }

    stages {
        stage('Verify Environment') {
            steps {
                script {
                    echo "Vérification de la configuration Docker..."
                    sh '''
                        docker --version || echo "Docker n'est pas installé!"
                    '''
                }
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    echo "Construction de l'image Docker..."
                    sh """
                        docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
                    """
                }
            }
        }

        stage('Docker Run Tests') {
            steps {
                script {
                    echo "Exécution des tests dans un conteneur Docker..."
                    sh """
                        docker run --rm ${DOCKER_IMAGE}:${DOCKER_TAG} java -version
                    """
                }
            }
        }

        stage('Docker Push') {
            steps {
                script {
                    echo "Publication de l'image Docker..."
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )]) {
                        sh """
                            echo \$DOCKER_PASSWORD | docker login -u \$DOCKER_USERNAME --password-stdin
                            docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
                        """
                    }
                }
            }
        }
    }

    post {
        always {
            echo "Nettoyage de l'espace de travail..."
            cleanWs()
        }
    }
}
