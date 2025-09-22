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

    extra_field_states = [
      'Consumer may ignore extra fields',
      'Provider may include extra fields'
    ]
    if req.post? && req.path == '/tax/calc' && extra_field_states.include?(ENV['PACT_PROVIDER_STATE'])
      original = +""
      body.each { |chunk| original << chunk }
      begin
        json = JSON.parse(original)
        json['ignored'] ||= 'anything'
        new_body = [JSON.generate(json)]
        headers['Content-Length'] = new_body.first.bytesize.to_s
        return [status, headers, new_body]
      rescue JSON::ParserError
      end
    end

    [status, headers, body]
  end
end

PACT_PATH = File.expand_path('../../pacts/orders-tax.json', __dir__)

Pact.service_provider 'tax' do
  app { PactResponseAugmenter.new(Sinatra::Application) }

  honours_pact_with 'orders' do
    pact_uri PACT_PATH
  end
end

Pact.provider_states_for 'orders' do
  provider_state 'Tax service can compute tax for USD' do
    set_up do
      ENV['PACT_PROVIDER_STATE'] = 'Tax service can compute tax for USD'
    end
  end

  provider_state 'Consumer may ignore extra fields' do
    set_up do
      ENV['PACT_PROVIDER_STATE'] = 'Consumer may ignore extra fields'
    end
  end

  # Backwards/compat alias with the wording in the current pact file
  provider_state 'Provider may include extra fields' do
    set_up do
      ENV['PACT_PROVIDER_STATE'] = 'Provider may include extra fields'
    end
  end
end
