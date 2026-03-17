# Loan Application Evaluator

A Spring Boot REST service that evaluates loan applications and determines whether a loan offer can be approved, built for the RBIH take-home assignment.

## Tech Stack

- Java 17
- Spring Boot 3.5.11
- Spring Data JPA + H2 (in-memory)
- Jakarta Bean Validation
- Lombok
- JUnit 5

## Running the Application

```bash
mvn spring-boot:run
```

The service starts on `http://localhost:8080`.

## API

### POST /applications

Submit a loan application for evaluation.

**Request:**
```json
{
  "applicant": {
    "name": "Aravind",
    "age": 30,
    "monthlyIncome": 75000,
    "employmentType": "SALARIED",
    "creditScore": 760
  },
  "loan": {
    "amount": 500000,
    "tenureMonths": 36,
    "purpose": "PERSONAL"
  }
}
```

**Approved Response (201):**
```json
{
  "applicationId": "uuid",
  "status": "APPROVED",
  "riskBand": "LOW",
  "offer": {
    "interestRate": 12.00,
    "tenureMonths": 36,
    "emi": 16607.15,
    "totalPayable": 597857.40
  }
}
```

**Rejected Response (201):**
```json
{
  "applicationId": "uuid",
  "status": "REJECTED",
  "rejectionReasons": ["LOW_CREDIT_SCORE"]
}
```

**Validation Error (400):**
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": [
    {
      "field": "applicant.age",
      "message": "Age must be at least 21"
    }
  ],
  "timestamp": "2026-03-18T10:30:00"
}
```

## Running Tests

```bash
mvn test
```

## Project Structure

See [DEVELOPMENT_NOTES.md](DEVELOPMENT_NOTES.md) for architecture decisions, trade-offs, and design patterns used.