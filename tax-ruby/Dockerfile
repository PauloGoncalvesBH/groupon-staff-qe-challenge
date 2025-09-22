FROM ruby:3.2-alpine@sha256:a9cc6b6548d6268c249e5f2f571a16549ed5448e7dc7152a6b22b33d631235aa AS base
ENV APP_HOME=/app
WORKDIR $APP_HOME

# hadolint ignore=DL3018
RUN apk add --no-cache build-base curl

COPY Gemfile* ./
RUN bundle install --jobs 4 --retry 3

COPY . .

# Non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup \
  && chown -R appuser:appgroup $APP_HOME
USER appuser

EXPOSE 4567

ENV RACK_ENV=production
CMD ["bundle", "exec", "puma", "-b", "tcp://0.0.0.0:4567"]
