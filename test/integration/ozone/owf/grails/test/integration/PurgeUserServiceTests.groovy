package ozone.owf.grails.test.integration

import ozone.owf.grails.domain.Person

class PurgeUserServiceTests extends GroovyTestCase {
    def purgeUserService

    def testUser1
    def testUser2
    def dummyServiceItem
    def systemUser
    def originalUserCount

    protected void setUp() {
        super.setUp()

        testUser1 = new Person(username: 'purgeUser1', displayName: 'Toby Purged', createdDate: new Date())
        testUser1.save(flush:true)

        testUser2 = new Person(username: 'keepUser2', displayName: 'Dont Purge', createdDate: new Date())
        testUser2.save(flush:true)

        originalUserCount = Person.count()
    }

    void testPurgePerson() {
        createPerson(testUser1)
        Person ownedByOtherUser = createPerson(testUser2)
        assertEquals originalUserCount+2, Person.count()
        assertNotNull Person.findByUsername(testUser1.username)
        assertNotNull Person.findByUsername(testUser2.username)

        purgeUserService.purgeUser(Person.findByUsername(testUser1.username))
        assertEquals originalUserCount+1, Person.count()
        assertNull Person.findByUsername(testUser1.username)
        assertEquals ownedByOtherUser, Person.findByUsername(testUser2.username)
    }

    void testGetInactiveAccounts_NoUsers() {
        def inactiveAccounts = purgeUserService.getInactiveAccounts(90)
        assertEquals 0, inactiveAccounts.size()
    }

    void testGetInactiveAccounts_SelectsCorrectUsers() {
        def today = new Date()
        def thresholdInDays = 90
        def inactiveUser = createPersonWithLastLogin(today.minus(thresholdInDays+1))
        createPersonWithLastLogin(today.minus(thresholdInDays-1))

        def inactiveAccounts = purgeUserService.getInactiveAccounts(thresholdInDays)
        assertEquals 1, inactiveAccounts.size()
        assertEquals inactiveUser, inactiveAccounts[0]
    }

    void testPurgeInactiveUsers() {
        def today = new Date()
        def thresholdInDays = 90
        def inactiveUser = createPersonWithLastLogin(today.minus(thresholdInDays+1))
        def activeUser = createPersonWithLastLogin(today.minus(thresholdInDays-1))

        purgeUserService.purgeInactiveAccounts()

        assertNull Person.findByUsername(inactiveUser.username)
        assertNotNull Person.findByUsername(activeUser.username)
    }

    private Person createPerson(Person user) {
        Person person = Person.build(username: user.username)
        return person
    }

    private Person createPersonWithLastLogin(Date lastLoginDate) {
        Person person = Person.build(lastLogin: lastLoginDate)
        return person
    }
}