pipeline {
    agent any

    tools {
        maven 'Maven_3.8.5'
        jdk 'OpenJDK_17'
    }

    environment {
        DOCKER_IMAGE = "rimsdk/banking-app"
        DOCKER_TAG = "latest"
    }

    stages {
        stage('Verify Tools') {
            steps {
                echo "Vérification des outils Maven et Java..."
                sh 'mvn --version' // Vérifie que Maven est bien configuré
                sh 'java -version' // Vérifie que le JDK est bien configuré
            }
        }

        stage('Clone') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                script {
                    echo "Construction du projet avec Maven..."
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Test') {
            steps {
                echo "Exécution des tests unitaires avec Maven..."
                sh 'mvn test'
            }
            post {
                always {
                    echo "Publication des résultats des tests..."
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials',
                                                    usernameVariable: 'DOCKER_USERNAME',
                                                    passwordVariable: 'DOCKER_PASSWORD')]) {
                        echo "Construction et publication de l'image Docker..."
                        sh """
                            docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .

                            echo "Connexion à DockerHub..."
                            echo \$DOCKER_PASSWORD | docker login -u \$DOCKER_USERNAME --password-stdin

                            echo "Publication de l'image Docker..."
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
