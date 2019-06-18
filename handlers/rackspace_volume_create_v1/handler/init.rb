require File.expand_path(File.join(File.dirname(__FILE__), 'dependencies'))

class RackspaceVolumeCreateV1
  def initialize(input)
    # Set the input document attribute
    @input_document = REXML::Document.new(input)
    
    # Store the info values in a Hash of info names to values.
    @info_values = {}
    REXML::XPath.each(@input_document,"/handler/infos/info") { |item|
      @info_values[item.attributes['name']] = item.text
    }
    
    # Retrieve all of the handler parameters and store them in a hash attribute
    # named @parameters.
    @parameters = {}
    REXML::XPath.match(@input_document, 'handler/parameters/parameter').each do |node|
      @parameters[node.attribute('name').value] = node.text.to_s
    end

    @enable_debug_logging = @info_values['enable_debug_logging'] == 'Yes'
  end
  
  def execute()
    puts "Initializing fog the service" if @enable_debug_logging
    service_options = {
      :rackspace_username => "kineticdata",
      :rackspace_api_key => "78d3b5e585ef252dfd10cd4b2f1eddbe"
    }
    if @parameters['rackspace_region'] != ""
      service_options[:rackspace_region] = @parameters['rackspace_region'].to_sym
    end
    service = Fog::Rackspace::BlockStorage.new(service_options)

    puts "Creating the new volume" if @enable_debug_logging
    volume = service.volumes.create(:display_name => @parameters['display_name'],
      :volume_type => @parameters['volume_type'], :size => @parameters['size'])

    puts "Returning the results" if @enable_debug_logging
    <<-RESULTS
    <results>
      <result name="volume_id">#{volume.id}</result>
      <result name="volume_state">#{volume.state}</result>
    </results>
    RESULTS
  end

  # This is a template method that is used to escape results values (returned in
  # execute) that would cause the XML to be invalid.  This method is not
  # necessary if values do not contain character that have special meaning in
  # XML (&, ", <, and >), however it is a good practice to use it for all return
  # variable results in case the value could include one of those characters in
  # the future.  This method can be copied and reused between handlers.
  def escape(string)
    # Globally replace characters based on the ESCAPE_CHARACTERS constant
    string.to_s.gsub(/[&"><]/) { |special| ESCAPE_CHARACTERS[special] } if string
  end
  # This is a ruby constant that is used by the escape method
  ESCAPE_CHARACTERS = {'&'=>'&amp;', '>'=>'&gt;', '<'=>'&lt;', '"' => '&quot;'}
end
