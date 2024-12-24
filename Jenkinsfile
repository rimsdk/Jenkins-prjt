pipeline {
    agent any

    tools {
        maven 'Maven_3.8.5' // Assurez-vous que Maven est correctement configuré dans Jenkins
    }

    environment {
        JAVA_HOME = '/opt/java/openjdk'
        PATH = "${env.JAVA_HOME}/bin:${env.PATH}"
        DOCKER_IMAGE = "rimsdk/banking-app"
        DOCKER_TAG = "latest"
    }

    stages {
        stage('Verify Environment') {
            steps {
                script {
                    echo "Vérification de l'environnement..."
                    sh '''
                        java -version || echo "Java n'est pas installé!"
                        mvn --version || echo "Maven n'est pas installé!"
                        docker --version || echo "Docker n'est pas installé!"
                    '''
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    echo "Construction de l'application avec Maven..."
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    echo "Exécution des tests unitaires..."
                    sh 'mvn test'
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                script {
                    echo "Construction et publication de l'image Docker..."
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-credentials', // Remplacez par l'ID de vos credentials Docker Hub
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )]) {
                        sh """
                            docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
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
            cleanWs() // Nettoie l'espace de travail après exécution
        }
    }
}
