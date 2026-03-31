EKS CI/CD Pipeline with GitHub Actions, ECR, EKS, Blue-Green & Canary
📖 Overview

This project demonstrates a complete CI/CD pipeline that automates the process of building, storing, and deploying a containerized Java application into Amazon EKS.

The system follows modern DevOps principles:

Infrastructure as Code (Terraform – already provisioned)
Secure authentication using OIDC (no AWS keys)
Container-based deployment using Docker
Kubernetes orchestration using EKS
Advanced deployment strategies (Blue-Green & Canary)
Monitoring using Prometheus & Grafana

The pipeline ensures that every code change is automatically built, pushed, and deployed without manual intervention while maintaining reliability and observability.

🏗️ Architecture Flow

GitHub Repository (Application Code)
↓
GitHub Actions (CI/CD Pipeline Execution)
↓
OIDC Authentication
↓
AWS IAM Role Assumption
↓
Docker Build (Container Image Creation)
↓
Amazon ECR (Private Image Registry)
↓
Amazon EKS Cluster
↓
Kubernetes Pods (Running Containers)
↓
Kubernetes Service (NodePort / LoadBalancer)
↓
AWS Application Load Balancer
↓
End User (Browser Access)

This architecture shows how code moves from development to production in a fully automated and secure manner. GitHub triggers the pipeline, which builds and stores container images in ECR. Kubernetes then pulls and runs these images. Services and ALB expose the application externally. Monitoring ensures visibility into runtime behavior.

🔄 Pipeline Execution Flow

Code Trigger

Any push to the master branch automatically triggers the GitHub Actions workflow. This ensures continuous integration and eliminates manual deployment steps.

Authentication (OIDC-Based Access)

Instead of storing AWS credentials, the pipeline uses OIDC:

GitHub generates a temporary token
AWS validates the token via OIDC provider
IAM role github-actions-eks-role is assumed

This provides:

No hardcoded secrets
Temporary credentials
Secure access model
Build Phase (Docker Image Creation)

The pipeline builds the Docker image:

Java application is packaged using Maven
Dependencies are resolved
Docker image is created
Image is tagged using commit SHA
docker build -t dev-java-app:<commit-id> .
Push Phase (ECR Integration)

The Docker image is pushed to Amazon ECR:

docker push 816069164153.dkr.ecr.us-east-1.amazonaws.com/dev-java-app:<tag>

ECR acts as:

Private container registry
Central image storage
Source for Kubernetes deployments
Cluster Access (EKS Connection)
aws eks update-kubeconfig --region us-east-1 --name EKS-DEV

This allows GitHub Actions to execute kubectl commands directly on the cluster.

Deployment Phase (Kubernetes Apply)
kubectl apply -f k8s/

This creates:

Deployment → manages pods
Service → exposes application
Rollout → manages canary strategy
Runtime Execution (EKS)

Initially, deployment failed with ImagePullBackOff.

Pods were created and entered Running state.

Key idea:

Docker builds the image
Kubernetes runs the container
External Exposure (NodePort → ALB)

Initially exposed using NodePort.

Later exposed using AWS Load Balancer.

🔁 Data Flow (End-to-End System Movement)
GitHub → CI Pipeline → Docker Image → ECR → EKS → Pods → Service → ALB → User
🔵🟢 Blue-Green Deployment

Blue → stable version
Green → new version

Traffic switches safely between versions.

🟡 Canary Deployment

Traffic is gradually shifted to the new version while monitoring system behavior.

🚀 Argo Rollouts (Progressive Delivery)

Traffic shifts in controlled stages (20% → 50% → 100%).

📊 Monitoring (Prometheus + Grafana)

Metrics collected and visualized for system health.

🧠 Concepts Demonstrated
CI/CD automation using GitHub Actions
Secure AWS authentication with OIDC
Docker image lifecycle management
Kubernetes deployments and services
Blue-Green and Canary deployment strategies
Monitoring with Prometheus & Grafana
👩‍💻 Author

Asha 

⭐ Summary

This project showcases a real-world DevOps pipeline where CI/CD, security, containerization, Kubernetes, and monitoring work together to deliver a fully automated and production-ready deployment system.
