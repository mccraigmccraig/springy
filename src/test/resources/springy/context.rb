defaults :init_method =>"initialise", :destroy_method=>"destroy"

require('springy/config.rb')

bean "org.springframework.beans.factory.config.MethodInvokingFactoryBean" do |b|
    b.targetClass "springy.beans.Bean1"
    b.targetMethod "staticMethod"
end

#slightly more elegant
static_bean "springy.beans.Bean1" do |b|
    b.staticMethodWithParameters(20, "a String", :bean2)
    #b.aStaticField = 200
end

# verbose property syntax
bean :'bean1-verbose', "springy.beans.Bean1" do |b|
    b.property :name => "bean2", :ref => "bean2"
    b.property :name => "listProperty", :value => ["1","2","3"]
    b.property :name => "booleanProperty", :value => true
end

# short property syntax + initialiser
bean :bean1, "springy.beans.Bean1" do
    bean2 :bean2
    listProperty ["1","2","3"]
    initialiser do |instance|
        instance.do_something(22)
    end
    booleanProperty true
end

# constructor init
bean :bean2, "springy.beans.Bean2" do
    new "Max", "Pierre", A_config_setting
end

# constructor + property init using references
bean(:bean3, "springy.beans.Bean3") {
    new "Vic", :bean2
    parameters [:bean1, "A String"]
}

# hashmaps + inner beans
bean :bean4, "springy.beans.Bean4" do
    new("Steve",
        { "foo" => "baz",
          "bean1" => :bean1,
          "bean2" => lambda {
            bean("springy.beans.Bean2") { new("Gaga", "Baba", 23) }
          }
        })
    anotherBean {
     bean("springy.beans.Bean3") { new("Pete",:bean2) }
    }
end

bean :bean4_yaml, "springy.beans.Bean4" do |b|
    b.new("Steve", yaml("/springy/a_map.yml")) 
end

bean :bean5, "springy.beans.Bean5", :scope=>'prototype', :init_method=>'myInit', :destroy_method=>'myDestroy', :dependency_check=>'simple', :lazy_init=>true, :abstract=>true, :autowire=>'byType'

# aliasing
bean_alias :bean4, :'bean4-alias'

inline_xml do <<XML
    <beans>
        <bean id="bean1-2" class="springy.beans.Bean1"/>
    </beans>
XML
end

inline_xml(%q{<beans><bean id="bean1-3" class="springy.beans.Bean1"/></beans>})

before_init do |b, n|
    b.set_before_init_called if b.respond_to?(:set_before_init_called)
end