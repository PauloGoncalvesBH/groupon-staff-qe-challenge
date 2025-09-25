require 'pact/provider/rspec'
require_relative '../../app'
require 'json'

class PactResponseAugmenter
  def initialize(app)
    @app = app
  end

  def call(env)
    status, headers, body = @app.call(env)
    req = Rack::Request.new(env)

    # Inject extra field based on state configured in provider state set_up
    if req.post? && req.path == '/tax/calc' && ENV['PACT_INJECT_IGNORED']
      original = +""
      body.each { |chunk| original << chunk }
      begin
        json = JSON.parse(original)
        json['ignored'] ||= ENV['PACT_INJECT_IGNORED']
        new_body = [JSON.generate(json)]
        headers['Content-Length'] = new_body.first.bytesize.to_s
        return [status, headers, new_body]
      rescue JSON::ParserError
      end
    end

    [status, headers, body]
  end
end

PACT_PATH = ENV.fetch('PACT_FILE')

Pact.service_provider 'tax' do
  app { PactResponseAugmenter.new(Sinatra::Application) }

  honours_pact_with 'orders' do
    pact_uri PACT_PATH
  end
end

Pact.provider_states_for 'orders' do
  provider_state 'Consumer will send extra non-breaking fields' do
    set_up do
      ENV['PACT_INJECT_IGNORED'] = 'anything'
    end
  end
end
