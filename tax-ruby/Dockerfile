FROM ruby:3.2-alpine AS base
ENV APP_HOME=/app
WORKDIR $APP_HOME

RUN apk add --no-cache build-base

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
