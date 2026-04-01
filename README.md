# 🚀 EKS CI/CD Pipeline with GitHub Actions, Terraform & AWS

## 📖 Overview

This project demonstrates a complete CI/CD pipeline that automates the process of building, storing, and deploying a containerized application to Amazon EKS.

The setup follows modern DevOps principles:
* **Infrastructure as Code:** Terraform
* **Secure Authentication:** OIDC using GitHub Actions (no AWS keys)
* **Container-based Deployment:** Docker
* **Kubernetes Orchestration:** Amazon EKS

The pipeline ensures that every code change is automatically built and deployed without manual intervention.

---

## 🏗️ Architecture Flow

```text
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
Amazon EKS Control Plane
 ↓
Worker Nodes (Kubernetes Pods)
 ↓
Kubernetes Service (LoadBalancer)
 ↓
AWS Elastic Load Balancer (Public Endpoint)
 ↓
End User (Browser Access)
Architecture Overview: Visualizes the high-level architecture and data flow, showing how code pushed to GitHub triggers an OIDC-authenticated GitHub Action to build, push, and deploy to EKS.

🔄 Pipeline Execution Flow
1. Code Trigger
Any push to the master branch triggers the GitHub Actions workflow automatically.

Audit trail showing automated workflow triggers upon code pushes.

2. Authentication (OIDC-Based Access)
Instead of storing AWS credentials, the pipeline uses OIDC:

GitHub generates a temporary token.

AWS validates the token using the OIDC provider.

IAM role github-actions-eks-role is assumed.

This approach provides: Zero hardcoded secrets, temporary access tokens, and a highly secure authentication model.

3. Build Phase (Docker Image Creation)
The GitHub Actions runner builds the Docker image:

Application source is packaged.

Dependencies are installed.

Image is created and tagged.

Bash
docker build -t dev-eks-app .
Detailed view of the automated build steps executed by the pipeline.

4. Push Phase (ECR Integration)
The Docker image is pushed to Amazon ECR:

Bash
docker push [816069164153.dkr.ecr.us-east-1.amazonaws.com/dev-eks-app:latest](https://816069164153.dkr.ecr.us-east-1.amazonaws.com/dev-eks-app:latest)
ECR acts as a private container registry, central storage for images, and the primary source for Kubernetes deployments.

Amazon ECR acting as the secure, private container registry.

Versioned Docker images stored immutably using commit SHAs.

5. Cluster Access (EKS Connection)
The pipeline connects to the Kubernetes cluster:

Bash
aws eks update-kubeconfig --region us-east-1 --name EKS-DEV
This allows GitHub Actions to securely run kubectl commands.

6. Deployment Phase (Kubernetes Apply)
Kubernetes manifests are applied:

Bash
kubectl apply -f k8s/
This creates the Deployment (manages Pods) and the Service (exposes the application).

7. Runtime Execution (EKS)
Kubernetes pulls the image from ECR.

Pods are created inside worker nodes.

Containers start running the application.

Key idea: Docker builds the image → Kubernetes runs the container.

Verifying pod status and handling real-world runtime execution states.

8. External Exposure (LoadBalancer)
A Kubernetes Service of type LoadBalancer ensures:

AWS provisions an ELB automatically.

Traffic is routed to Pods.

The application becomes publicly accessible.

Validating external traffic routing and application exposure.

🔐 Access Control & Security
IAM Role (GitHub Actions)
Created via Terraform.

Assumed using OIDC.

Grants access to ECR (push images) and EKS (cluster operations).

Kubernetes RBAC Mapping
The IAM role is mapped in aws-auth:

YAML
mapRoles:
  - rolearn: arn:aws:iam::816069164153:role/github-actions-eks-role
    username: github-actions
    groups:
      - system:masters
This enables deployment access directly inside the cluster.

🧩 Infrastructure Setup (Terraform)
The infrastructure is modular and reusable, supporting environment-based deployments (dev, sit, prod):

VPC: Networking layer.

EKS Cluster: Control plane.

Node Groups: Compute layer.

ECR Repository: Image storage.

IAM Roles: Security.

📸 Project Screenshots
Repository Structure:

CI/CD Pipeline Success:

Pipeline Execution Steps:

ECR Repository:

ECR Image:

Pods and Service (External IP):

Application Output:

Cluster Resource Monitoring (Grafana):

Deployment Traffic Monitoring (Grafana):

🌐 Application Access
http://<REPLACE_WITH_YOUR_ELB_ENDPOINT>

🧠 Concepts Demonstrated
CI/CD automation using GitHub Actions

Secure AWS authentication with OIDC

Docker image lifecycle management

Kubernetes Deployment and Service usage

Infrastructure provisioning with Terraform

🚀 Possible Enhancements
Helm-based deployments

Multi-environment pipelines

HTTPS using Ingress + ACM

Monitoring (Prometheus & Grafana)

Versioned image tagging strategy

👩‍💻 Author
Asha

⭐ Summary
This project showcases a real-world DevOps pipeline where infrastructure, security, CI/CD, and Kubernetes work together to deliver a fully automated deployment system.
