# JobPulse Spring Boot Starter

Automatic monitoring for scheduled jobs in Spring Boot applications. Add one
dependency and every `@Scheduled` job is discovered on startup, tracked on each
run, and reported with success/failure status, duration, and error details.

No curl commands. No ping URLs to paste. Zero code changes.

> 👀 **Join the beta and lock in 30% off for life:** [jobpulse.clcai.cn](https://jobpulse.clcai.cn)

## Quick start

```xml
<dependency>
  <groupId>io.jobpulse</groupId>
  <artifactId>jobpulse-spring-boot-starter</artifactId>
  <version>0.1.0</version>
</dependency>
```

```yaml
jobpulse:
  api-key: ${JOBPULSE_API_KEY}
  log-only: true   # log events locally instead of sending them (great for trying it out)
```

Start your app. You will see log lines like:

```
[jobpulse] POST /v1/jobs/register {"app":"my-app","jobKey":"com.acme.jobs.Reports#nightly","schedule":"cron:0 0 2 * * *", ...}
[jobpulse] POST /v1/executions {"app":"my-app","jobKey":"com.acme.jobs.Reports#nightly","status":"SUCCESS","durationMs":4213, ...}
```

## Configuration

| Property | Default | Description |
|---|---|---|
| `jobpulse.enabled` | `true` | Set to `false` to disable the starter completely. |
| `jobpulse.api-url` | `https://ingest.jobpulse.io` | Ingest endpoint base URL. |
| `jobpulse.api-key` | — | API key for the hosted service. Reporting is disabled when blank. |
| `jobpulse.app-name` | `spring.application.name` | Logical name of your application. |
| `jobpulse.environment` | `default` | Environment label (e.g. `prod`, `staging`). |
| `jobpulse.log-only` | `false` | Log events locally instead of sending them. |

## How it works

The starter wraps every `TaskScheduler` bean in your application context
(Spring Boot auto-configures one by default). When Spring's scheduler machinery
schedules a job, the wrapper records its identity and schedule, then decorates
the run to report:

- **Registration** — job key (`com.acme.jobs.Reports#nightly`) and schedule
  (`cron:...`, `fixedRate:PT5M`, `fixedDelay:...`) sent once on first sight.
- **Executions** — status, start time, duration, and on failure the exception
  class and message.

Reports are sent asynchronously on a dedicated background thread with a bounded
queue. If JobPulse is unreachable, jobs run exactly as before — the starter
never throws into your application.

Only job metadata ever leaves your application: names, schedules, status,
timing, and error classes. Your business data does not.

## Requirements

- Java 17+
- Spring Boot 3.x
- A `TaskScheduler` bean (Spring Boot provides one automatically)

## Dashboard & Alerting

Once your jobs are reporting, view them at:
`https://jobpulse.clcai.cn/v1/dashboard`

Enter your ingest token to see all tracked jobs, recent executions, and failures.

Set up webhook alerts so you get notified the moment a job fails (deduplicated per 30 minutes):

``bash
curl -X POST https://jobpulse.clcai.cn/v1/alerts/config \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"app":"my-app","env":"prod","webhookUrl":"https://hooks.slack.com/..."}'
``n
## Roadmap

- Quartz job auto-discovery
- Email alerts
- Silence detection (notify when a job stops reporting)
- Micrometer metrics
- Self-hosted dashboard option

## License

[MIT](LICENSE)
