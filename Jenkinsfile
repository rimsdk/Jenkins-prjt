pipeline {
    agent {
        docker {
            image 'maven:3.8.5-openjdk-17-slim' // Utilisation de Maven avec Java 17
            args '--privileged -v /var/run/docker.sock:/var/run/docker.sock' // Accès au socket Docker
        }
    }

    environment {
        DOCKER_IMAGE = "rimsdk/banking-app"
        DOCKER_TAG = "latest"
    }

    stages {
        stage('Build') {
            steps {
                echo "Construction de l'application avec Maven..."
                sh 'mvn clean package'
            }
        }

        stage('Test') {
            steps {
                echo "Exécution des tests unitaires..."
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml' // Génération des rapports de tests
                }
            }
        }

        stage('Prepare Docker') {
            steps {
                echo "Installation de Docker CLI..."
                sh '''
                    apt-get update && apt-get install -y docker.io
                    docker --version
                '''
            }
        }

        stage('Docker Build & Push') {
            steps {
                echo "Construction et publication de l'image Docker..."
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USERNAME',
                    passwordVariable: 'DOCKER_PASSWORD'
                )]) {
                    sh '''
                        docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
                        echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin
                        docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
                    '''
                }
            }
        }
    }

    post {
        always {
            echo "Nettoyage de l'environnement de travail..."
            cleanWs()
        }
    }
}
