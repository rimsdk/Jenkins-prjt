pipeline {
    agent any

    tools {
        maven 'Maven_3.8.5'
        jdk 'OpenJDK_17'
    }

    environment {
        JAVA_HOME = "C:/Program Files/Eclipse Adoptium/jdk-17.0.14.2-hotspot"  // Utilisation des barres obliques
        PATH = "${JAVA_HOME}/bin:${env.PATH}"  // Mise à jour de la variable PATH
        DOCKER_IMAGE = "rimsdk/banking-app"
        DOCKER_TAG = "latest"
    }

    stages {
        stage('Verify Environment') {
            steps {
                script {
                    echo "Vérification de la configuration Java et Maven..."
                    sh 'echo $JAVA_HOME'  // Affiche la variable d'environnement JAVA_HOME
                    sh 'java -version'    // Vérifie la version de Java
                    sh 'mvn --version'    // Vérifie la version de Maven
                }
            }
        }

        stage('Clone Repository') {
            steps {
                echo "Clonage du dépôt Git..."
                checkout scm  // Récupère le code source du dépôt
            }
        }

        stage('Build') {
            steps {
                script {
                    echo "Construction de l'application avec Maven..."
                    sh 'mvn clean package -DskipTests'  // Exécute la commande de build
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    echo "Exécution des tests unitaires..."
                    sh 'mvn test'  // Lance les tests Maven
                }
            }
            post {
                always {
                    echo "Génération du rapport de tests..."
                    junit '**/target/surefire-reports/*.xml'  // Génère les rapports de tests
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
