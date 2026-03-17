# Development Notes — Loan Application Evaluator

## Overall Approach

Built a Spring Boot 3.5.11 REST service (Java 17) following a layered architecture with clear separation of concerns:

1. **Controller Layer** — Thin controller handling HTTP concerns only. Receives the request, triggers validation, delegates to service, returns the response with appropriate status code.
2. **DTO Layer** — Lombok-based classes with Jakarta Bean Validation annotations for input validation. Separate request and response DTOs. Response DTOs are split into `LoanApplicationApprovedResponse` and `LoanApplicationRejectedResponse` implementing a common `LoanApplicationResponse` marker interface for type-safe polymorphic returns.
3. **Service Layer** — `LoanApplicationService` acts as the pipeline orchestrator, coordinating eligibility evaluation, risk classification, interest rate calculation, and offer generation. All business logic components are organized as sub-packages under `service/`:
   - `service/calculator/` — EMI calculation with BigDecimal precision
   - `service/rule/` — Eligibility rules following the Chain of Responsibility pattern
   - `service/engine/` — Interest rate computation, risk classification, and offer generation
4. **Model Layer** — JPA entity with Lombok `@Builder` for clean construction, persisted to H2 for audit.
5. **Repository Layer** — Spring Data JPA repository for persisting loan decisions.
6. **Exception Layer** — `@RestControllerAdvice` based global exception handler for structured error responses.

## Key Design Decisions

### Design Patterns

- **Strategy Pattern** — `PremiumCalculator` interface with three implementations (`RiskPremiumCalculator`, `EmploymentPremiumCalculator`, `LoanSizePremiumCalculator`). The `InterestRateEngine` composes them via `List<PremiumCalculator>` injection. Adding a new premium type requires only creating a new `@Component` — no existing code changes needed.

- **Chain of Responsibility** — `EligibilityRule` interface with three implementations. The `EligibilityEvaluator` iterates all rules without short-circuiting, collecting every applicable rejection reason. This ensures the API response is informative — applicants see all reasons for rejection, not just the first one encountered. Rules are ordered via `@Order` annotations for deterministic evaluation.

- **Builder Pattern** — Used via Lombok `@Builder` on the `LoanApplication` JPA entity, `OfferDto`, and both response classes. Provides clean, readable object construction without telescoping constructors.

- **Marker Interface** — `LoanApplicationResponse` interface implemented by both `LoanApplicationApprovedResponse` and `LoanApplicationRejectedResponse`. Enables type-safe polymorphic returns from the service layer without resorting to `Object` as a return type.

### SOLID Principles

- **Single Responsibility** — Each eligibility rule, each premium calculator, and each engine component has exactly one job. The service only orchestrates.
- **Open/Closed** — Adding a new eligibility rule or premium type requires creating a new `@Component` class. No existing code needs modification — Spring auto-discovers and injects the new bean into the existing lists.
- **Liskov Substitution** — All `EligibilityRule` and `PremiumCalculator` implementations are interchangeable. Both response types are substitutable wherever `LoanApplicationResponse` is expected.
- **Interface Segregation** — `EligibilityRule` and `PremiumCalculator` are narrow, focused interfaces with a single method each.
- **Dependency Inversion** — The service layer depends on abstractions (`List<EligibilityRule>`, `List<PremiumCalculator>`), not concrete implementations.

### Separate Response Classes

Instead of a single response class with `@JsonInclude(NON_NULL)` to hide null fields, the approved and rejected responses are separate classes. Each class contains only the fields relevant to that outcome — no nulls, no conditional serialization. The `status` field is hardcoded as a `final` constant in each class, making it impossible to set incorrectly.

### Package Organization

Business logic components (`calculator/`, `rule/`, `engine/`) are organized as sub-packages under `service/` rather than top-level packages. This signals they are internal building blocks of the service layer — not independently usable outside of it.

### Financial Precision

All monetary calculations use `BigDecimal` with `MathContext(20, HALF_UP)` for intermediate precision and `scale = 2` for final results. This avoids floating-point rounding errors that are unacceptable in financial software.

### Two EMI Thresholds

The spec defines two distinct EMI thresholds:
1. **Eligibility gate (60%)** — Checked during eligibility evaluation at the base rate (12%). This is a broad filter.
2. **Offer gate (50%)** — Checked during offer generation at the final calculated rate (with all premiums). This is a stricter affordability check.

Both can independently cause rejection with different reason codes.

## Trade-offs Considered

| Decision | Alternative | Reasoning |
|---|---|---|
| H2 in-memory DB | PostgreSQL | Simpler setup for a take-home. Would use PostgreSQL in production with Flyway migrations. |
| Separate response classes | Single class with @JsonInclude | Avoids null fields entirely. Each response has exactly the fields the spec requires. More classes but cleaner design. |
| Marker interface | Abstract base class | No shared state or logic between response types. Interface keeps flexibility — classes can extend something else if needed. Abstract class would have an empty body. |
| `@Order` on rules | Custom ordering config | Simple and declarative. Would use a priority field or config-driven ordering for more complex rule engines. |
| `List<EligibilityRule>` injection | Rule engine framework (Drools) | The business rules are simple enough that a lightweight interface-based approach is clearer than introducing a heavyweight rule engine. |
| Sub-packages under service/ | Top-level packages | Calculator, rules, and engine are internal to the service layer. Nesting them signals this relationship in the package structure. |

## Assumptions

1. The eligibility EMI check at 60% uses the **base rate (12%)**, not the final rate with premiums. The spec states "Base Interest Rate = 12% annually" in the context of the EMI formula.
2. Loan amount `50,00,000` in Indian numbering = `5,000,000` (50 lakh).
3. `10,00,000` in the loan size premium threshold = `1,000,000` (10 lakh).
4. The `purpose` field is stored but does not currently affect eligibility or pricing. It is available for future business rules.
5. Application IDs are server-generated UUIDs (not client-provided).
6. The service returns HTTP 201 (Created) for both approved and rejected decisions, since a loan application record is created in both cases.
7. Age + tenure comparison uses `>` (strictly greater than 65), so exactly 65 is allowed.
8. The rejected response in the spec shows `"riskBand": null` — achieved naturally by having a separate rejected response class that simply doesn't include a `riskBand` field.

## Improvements With More Time

1. **API documentation** — Add Springdoc OpenAPI for auto-generated Swagger UI.
2. **Controller integration tests** — Add MockMvc tests verifying HTTP status codes, JSON structure, and validation error responses end-to-end.
3. **Database migrations** — Replace `ddl-auto` with Flyway for versioned schema management.
4. **GET endpoint** — Add `GET /applications` with pagination for audit record retrieval.
5. **Idempotency** — Add idempotency key support to prevent duplicate application submissions.
6. **Configurable thresholds** — Externalize business rule constants (60%, 50%, base rate, premium values) to `application.properties` for easier tuning without code changes.
7. **Audit enrichment** — Store the full calculation breakdown (base rate, each premium applied, intermediate EMI values) for regulatory transparency.
8. **Docker support** — Add Dockerfile and docker-compose.yml for containerized deployment.
9. **Logging** — Add structured logging with correlation IDs for request tracing across the evaluation pipeline.
10. **PostgreSQL** — Switch to PostgreSQL for production-grade persistence with proper indexing on applicationId and createdAt.