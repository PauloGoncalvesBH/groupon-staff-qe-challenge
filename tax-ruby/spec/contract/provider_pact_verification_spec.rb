# frozen_string_literal: true

require 'spec_helper'
require_relative './pact_helper'

PACT_PATH = File.expand_path('../../pacts/orders-tax.json', __dir__)

if File.exist?(PACT_PATH)
  RSpec.describe 'Pact provider verification', :contract do
    Pact.provider_verifier do |_verifier|
      # configuration lives in pact_helper.rb
    end
  end
else
  RSpec.describe 'Pact setup', :contract do
    it 'has consumer pact file generated' do
      raise "Pact file not found at #{PACT_PATH}. Generate it first (e.g., `make orders-test-contract`)."
    end
  end
end
