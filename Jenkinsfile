pipeline {
    agent any

    tools {
        maven 'Maven_3.8.5'
    }

    environment {
        JAVA_HOME = '/opt/java/openjdk'  // Use container's Java installation
        PATH = "${env.JAVA_HOME}/bin:${env.PATH}"
        DOCKER_IMAGE = "rimsdk/banking-app"
        DOCKER_TAG = "latest"
    }

    stages {
        stage('Verify Environment') {
            steps {
                script {
                    echo "Vérification de la configuration Java et Maven..."

                    // Use sh -l to ensure environment is properly loaded
                    sh '''
                        echo "Current Java installation:"
                        echo "JAVA_HOME=${JAVA_HOME}"
                        echo "PATH=${PATH}"
                        java -version
                        mvn --version
                    '''
                }
            }
        }

        stage('Clone Repository') {
            steps {
                echo "Clonage du dépôt Git..."
                checkout scm
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
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )]) {
                        sh """
                            echo "Construction de l'image Docker..."
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